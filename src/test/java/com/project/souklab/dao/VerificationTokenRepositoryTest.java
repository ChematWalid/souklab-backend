package com.project.souklab.dao;


import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.User;
import com.project.souklab.model.VerificationToken;
import com.project.souklab.model.VerificationTokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest slice verifying VerificationTokenRepository custom JPQL query findActiveToken
 * (temporal expiry, consumption exclusion, latest-token ordering) and the bulk modifying
 * invalidateActiveTokens query against an embedded H2 database.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VerificationTokenRepositoryTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 9, 5, 12, 0, 0);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    /**
     * Helper to persist an active user.
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
     * Helper to persist a verification token.
     */
    private VerificationToken persistToken(User user, VerificationTokenType type, String codeHash,
                                          LocalDateTime expiresAt, LocalDateTime usedAt) {
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .type(type)
                .codeHash(codeHash)
                .expiresAt(expiresAt)
                .usedAt(usedAt)
                .attempts(0)
                .build();
        return entityManager.persist(token);
    }

    /**
     * Verifies that findActiveToken returns the active token when usedAt is null and expiresAt is strictly in the future.
     */
    @Test
    @DisplayName("findActiveToken: returns active token when usedAt is null and expiresAt > now")
    void findActiveToken_whenValidAndUnexpired_returnsToken() {
        User user = persistUser("active_token@souklab.com");
        VerificationToken token = persistToken(
                user,
                VerificationTokenType.EMAIL_VERIFICATION,
                "hashed_code_123",
                FIXED_NOW.plusMinutes(15),
                null);

        entityManager.flush();
        entityManager.clear();

        Optional<VerificationToken> found = verificationTokenRepository.findActiveToken(
                user, VerificationTokenType.EMAIL_VERIFICATION, FIXED_NOW);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(token.getId());
        assertThat(found.get().getCodeHash()).isEqualTo("hashed_code_123");
        assertThat(found.get().getUsedAt()).isNull();
    }

    /**
     * Verifies that findActiveToken returns empty when the token has already been consumed (usedAt is not null).
     */
    @Test
    @DisplayName("findActiveToken: returns empty when token has already been consumed (usedAt != null)")
    void findActiveToken_whenAlreadyUsed_returnsEmpty() {
        User user = persistUser("used_token@souklab.com");
        persistToken(
                user,
                VerificationTokenType.EMAIL_VERIFICATION,
                "hashed_used_code",
                FIXED_NOW.plusMinutes(15),
                FIXED_NOW.minusMinutes(1));

        entityManager.flush();
        entityManager.clear();

        Optional<VerificationToken> found = verificationTokenRepository.findActiveToken(
                user, VerificationTokenType.EMAIL_VERIFICATION, FIXED_NOW);

        assertThat(found).isEmpty();
    }

    /**
     * Verifies that findActiveToken returns empty when the token has expired (expiresAt <= now).
     */
    @Test
    @DisplayName("findActiveToken: returns empty when expiresAt <= now (expired token)")
    void findActiveToken_whenExpired_returnsEmpty() {
        User user = persistUser("expired_token@souklab.com");

        persistToken(
                user,
                VerificationTokenType.EMAIL_VERIFICATION,
                "hashed_past_code",
                FIXED_NOW.minusSeconds(1),
                null);

        persistToken(
                user,
                VerificationTokenType.PASSWORD_RESET,
                "hashed_exact_now_code",
                FIXED_NOW,
                null);

        entityManager.flush();
        entityManager.clear();

        Optional<VerificationToken> pastToken = verificationTokenRepository.findActiveToken(
                user, VerificationTokenType.EMAIL_VERIFICATION, FIXED_NOW);
        assertThat(pastToken).isEmpty();

        Optional<VerificationToken> boundaryToken = verificationTokenRepository.findActiveToken(
                user, VerificationTokenType.PASSWORD_RESET, FIXED_NOW);
        assertThat(boundaryToken).isEmpty();
    }

    /**
     * Verifies that findActiveToken returns empty when type or user does not match.
     */
    @Test
    @DisplayName("findActiveToken: returns empty when user or token type does not match")
    void findActiveToken_whenUserOrTypeMismatch_returnsEmpty() {
        User owner = persistUser("owner_token@souklab.com");
        User otherUser = persistUser("other_token@souklab.com");

        persistToken(
                owner,
                VerificationTokenType.EMAIL_VERIFICATION,
                "hashed_token_owner",
                FIXED_NOW.plusMinutes(15),
                null);

        entityManager.flush();
        entityManager.clear();

        Optional<VerificationToken> wrongType = verificationTokenRepository.findActiveToken(
                owner, VerificationTokenType.PASSWORD_RESET, FIXED_NOW);
        assertThat(wrongType).isEmpty();

        Optional<VerificationToken> wrongUser = verificationTokenRepository.findActiveToken(
                otherUser, VerificationTokenType.EMAIL_VERIFICATION, FIXED_NOW);
        assertThat(wrongUser).isEmpty();
    }

    /**
     * Verifies that when multiple unexpired, unused tokens exist for the same user and type,
     * findActiveToken orders by createdAt DESC and returns the most recent one.
     */
    @Test
    @DisplayName("findActiveToken: orders by createdAt DESC returning the latest token when multiple active exist")
    void findActiveToken_whenMultipleActiveTokensExist_returnsMostRecentByCreatedAt() {
        User user = persistUser("multi_token@souklab.com");

        VerificationToken olderToken = persistToken(
                user,
                VerificationTokenType.EMAIL_VERIFICATION,
                "older_code_hash",
                FIXED_NOW.plusMinutes(10),
                null);

        VerificationToken newerToken = persistToken(
                user,
                VerificationTokenType.EMAIL_VERIFICATION,
                "newer_code_hash",
                FIXED_NOW.plusMinutes(15),
                null);

        entityManager.flush();

        entityManager.getEntityManager().createQuery(
                "UPDATE VerificationToken vt SET vt.createdAt = :ts WHERE vt.id = :id")
                .setParameter("ts", FIXED_NOW.minusMinutes(5))
                .setParameter("id", olderToken.getId())
                .executeUpdate();

        entityManager.getEntityManager().createQuery(
                "UPDATE VerificationToken vt SET vt.createdAt = :ts WHERE vt.id = :id")
                .setParameter("ts", FIXED_NOW.minusMinutes(1))
                .setParameter("id", newerToken.getId())
                .executeUpdate();

        entityManager.clear();

        Optional<VerificationToken> result = verificationTokenRepository.findActiveToken(
                user, VerificationTokenType.EMAIL_VERIFICATION, FIXED_NOW);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(newerToken.getId());
        assertThat(result.get().getCodeHash()).isEqualTo("newer_code_hash");
    }

    /**
     * Verifies that invalidateActiveTokens bulk-updates usedAt to the given timestamp on all unused tokens
     * for the target user and type, while leaving other users, other types, and already-used tokens untouched.
     */
    @Test
    @DisplayName("invalidateActiveTokens: marks all unused tokens as used for target user and type only")
    void invalidateActiveTokens_marksActiveTokensAsUsedForTargetUserAndTypeOnly() {
        User targetUser = persistUser("target_invalidate@souklab.com");
        User otherUser = persistUser("other_invalidate@souklab.com");

        VerificationToken targetToken1 = persistToken(
                targetUser,
                VerificationTokenType.EMAIL_VERIFICATION,
                "target_hash_1",
                FIXED_NOW.plusMinutes(15),
                null);

        VerificationToken targetToken2 = persistToken(
                targetUser,
                VerificationTokenType.EMAIL_VERIFICATION,
                "target_hash_2",
                FIXED_NOW.plusMinutes(30),
                null);

        LocalDateTime previousUsedAt = FIXED_NOW.minusHours(1);
        VerificationToken targetAlreadyUsed = persistToken(
                targetUser,
                VerificationTokenType.EMAIL_VERIFICATION,
                "target_hash_already_used",
                FIXED_NOW.plusMinutes(15),
                previousUsedAt);

        VerificationToken targetOtherType = persistToken(
                targetUser,
                VerificationTokenType.PASSWORD_RESET,
                "target_reset_hash",
                FIXED_NOW.plusMinutes(15),
                null);

        VerificationToken otherUserToken = persistToken(
                otherUser,
                VerificationTokenType.EMAIL_VERIFICATION,
                "other_user_hash",
                FIXED_NOW.plusMinutes(15),
                null);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime invalidateTimestamp = FIXED_NOW;
        verificationTokenRepository.invalidateActiveTokens(
                targetUser, VerificationTokenType.EMAIL_VERIFICATION, invalidateTimestamp);

        entityManager.clear();

        VerificationToken reloadedTarget1 = entityManager.find(VerificationToken.class, targetToken1.getId());
        assertThat(reloadedTarget1.getUsedAt()).isEqualTo(invalidateTimestamp);

        VerificationToken reloadedTarget2 = entityManager.find(VerificationToken.class, targetToken2.getId());
        assertThat(reloadedTarget2.getUsedAt()).isEqualTo(invalidateTimestamp);

        VerificationToken reloadedAlreadyUsed = entityManager.find(VerificationToken.class, targetAlreadyUsed.getId());
        assertThat(reloadedAlreadyUsed.getUsedAt()).isEqualTo(previousUsedAt);

        VerificationToken reloadedOtherType = entityManager.find(VerificationToken.class, targetOtherType.getId());
        assertThat(reloadedOtherType.getUsedAt()).isNull();

        VerificationToken reloadedOtherUser = entityManager.find(VerificationToken.class, otherUserToken.getId());
        assertThat(reloadedOtherUser.getUsedAt()).isNull();
    }
}
