package com.project.souklab.dao;


import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.RefreshToken;
import com.project.souklab.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest slice verifying RefreshTokenRepository query derivation, unique constraint enforcement
 * on the token column, and deletion operations against embedded H2.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RefreshTokenRepositoryTest {

    private static final Instant FIXED_EXPIRY = Instant.parse("2026-09-06T12:00:00Z");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Persists an active user.
     */
    private User persistUser(String email) {
        User user = User.builder()
                .email(email)
                .firstName("Test")
                .lastName("User")
                .status(AccountStatus.ACTIVE)
                .build();
        return entityManager.persist(user);
    }

    /**
     * Persists a refresh token for the given user.
     */
    private RefreshToken persistRefreshToken(User user, String tokenString, Instant expiryDate) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(tokenString)
                .expiryDate(expiryDate)
                .build();
        return entityManager.persist(token);
    }

    /**
     * Verifies that the unique constraint on the token column is enforced by the database,
     * throwing ConstraintViolationException when duplicate tokens are inserted across distinct users.
     */
    @Test
    @DisplayName("token unique constraint: throws ConstraintViolationException on duplicate token")
    void persistRefreshToken_whenDuplicateTokenString_throwsConstraintViolationException() {
        User user1 = persistUser("token_user1@souklab.com");
        User user2 = persistUser("token_user2@souklab.com");

        persistRefreshToken(user1, "duplicate-token-xyz", FIXED_EXPIRY);
        entityManager.flush();

        persistRefreshToken(user2, "duplicate-token-xyz", FIXED_EXPIRY.plusSeconds(3600));

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * Verifies findByToken returns the entity when token matches and empty Optional otherwise.
     */
    @Test
    @DisplayName("findByToken: returns token entity when matching, empty when absent")
    void findByToken_verifiesFoundAndNotFound() {
        User user = persistUser("find_token@souklab.com");
        RefreshToken token = persistRefreshToken(user, "valid-token-123", FIXED_EXPIRY);

        entityManager.flush();
        entityManager.clear();

        Optional<RefreshToken> found = refreshTokenRepository.findByToken("valid-token-123");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(token.getId());
        assertThat(found.get().getToken()).isEqualTo("valid-token-123");

        Optional<RefreshToken> absent = refreshTokenRepository.findByToken("non-existent-token");
        assertThat(absent).isEmpty();
    }

    /**
     * Verifies findByUser returns Optional containing the user's refresh token when present,
     * and Optional.empty() when the user has no associated refresh token.
     */
    @Test
    @DisplayName("findByUser: returns Optional containing token when present, empty Optional when absent")
    void findByUser_verifiesPresentAndAbsent() {
        User userWithToken = persistUser("has_token@souklab.com");
        User userWithoutToken = persistUser("no_token@souklab.com");

        RefreshToken token = persistRefreshToken(userWithToken, "user-token-abc", FIXED_EXPIRY);

        entityManager.flush();
        entityManager.clear();

        Optional<RefreshToken> found = refreshTokenRepository.findByUser(userWithToken);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(token.getId());

        Optional<RefreshToken> absent = refreshTokenRepository.findByUser(userWithoutToken);
        assertThat(absent).isEmpty();
    }

    /**
     * Verifies deleteByUser removes the user's refresh token from the database,
     * confirmed by a subsequent repository query returning empty after context clear.
     */
    @Test
    @DisplayName("deleteByUser: removes token entity from database")
    void deleteByUser_removesTokenFromDatabase() {
        User user = persistUser("delete_by_user@souklab.com");
        persistRefreshToken(user, "to-delete-user-token", FIXED_EXPIRY);

        entityManager.flush();
        entityManager.clear();

        refreshTokenRepository.deleteByUser(user);
        entityManager.flush();
        entityManager.clear();

        Optional<RefreshToken> afterDelete = refreshTokenRepository.findByUser(user);
        assertThat(afterDelete).isEmpty();
    }

    /**
     * Verifies deleteByToken removes the refresh token by its token string from the database,
     * confirmed by a subsequent repository query returning empty after context clear.
     */
    @Test
    @DisplayName("deleteByToken: removes token entity from database by token string")
    void deleteByToken_removesTokenFromDatabase() {
        User user = persistUser("delete_by_token@souklab.com");
        persistRefreshToken(user, "to-delete-token-str", FIXED_EXPIRY);

        entityManager.flush();
        entityManager.clear();

        refreshTokenRepository.deleteByToken("to-delete-token-str");
        entityManager.flush();
        entityManager.clear();

        Optional<RefreshToken> afterDelete = refreshTokenRepository.findByToken("to-delete-token-str");
        assertThat(afterDelete).isEmpty();
    }
}
