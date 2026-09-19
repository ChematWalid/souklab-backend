package com.project.souklab.dao;


import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.OAuthIdentity;
import com.project.souklab.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest slice verifying OAuthIdentityRepository query derivation, composite unique constraint
 * enforcement on (provider, provider_user_id), and multi-identity lookup per user against embedded H2.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OAuthIdentityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OAuthIdentityRepository oAuthIdentityRepository;

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
     * Persists an OAuthIdentity for the given user.
     */
    private OAuthIdentity persistIdentity(User user, String provider, String providerUserId, String email) {
        OAuthIdentity identity = OAuthIdentity.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .build();
        return entityManager.persist(identity);
    }

    /**
     * Verifies that the composite unique constraint on (provider, provider_user_id) is enforced
     * by the database, throwing ConstraintViolationException on duplicate combination.
     */
    @Test
    @DisplayName("composite unique constraint (provider, provider_user_id): throws ConstraintViolationException on duplicate")
    void persistOAuthIdentity_whenDuplicateProviderAndProviderUserId_throwsConstraintViolationException() {
        User user1 = persistUser("oauth_user1@souklab.com");
        User user2 = persistUser("oauth_user2@souklab.com");

        persistIdentity(user1, "google", "sub-123456", "oauth_user1@souklab.com");
        entityManager.flush();

        persistIdentity(user2, "google", "sub-123456", "oauth_user2@souklab.com");

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * Verifies findByProviderAndProviderUserId returns the matching identity when present,
     * and empty Optional when absent or criteria differ.
     */
    @Test
    @DisplayName("findByProviderAndProviderUserId: returns identity when matching, empty Optional when absent")
    void findByProviderAndProviderUserId_verifiesFoundAndNotFound() {
        User user = persistUser("oauth_find@souklab.com");
        OAuthIdentity identity = persistIdentity(user, "google", "sub-google-999", "oauth_find@souklab.com");

        entityManager.flush();
        entityManager.clear();

        Optional<OAuthIdentity> found = oAuthIdentityRepository.findByProviderAndProviderUserId("google", "sub-google-999");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(identity.getId());
        assertThat(found.get().getEmail()).isEqualTo("oauth_find@souklab.com");

        Optional<OAuthIdentity> wrongProvider = oAuthIdentityRepository.findByProviderAndProviderUserId("facebook", "sub-google-999");
        assertThat(wrongProvider).isEmpty();

        Optional<OAuthIdentity> wrongSub = oAuthIdentityRepository.findByProviderAndProviderUserId("google", "non-existent-sub");
        assertThat(wrongSub).isEmpty();
    }

    /**
     * Verifies findByUser returns a List containing all linked OAuth identities for that user,
     * returns an empty List when the user has no linked identities, and excludes other users' identities.
     */
    @Test
    @DisplayName("findByUser: returns List of all identities linked to user, empty List when none")
    void findByUser_returnsListOfIdentitiesLinkedToUser() {
        User userWithTwo = persistUser("multi_oauth@souklab.com");
        User userWithNone = persistUser("no_oauth@souklab.com");
        User otherUser = persistUser("other_oauth@souklab.com");

        OAuthIdentity googleIdentity = persistIdentity(userWithTwo, "google", "sub-google-001", "multi_oauth@souklab.com");
        OAuthIdentity githubIdentity = persistIdentity(userWithTwo, "github", "sub-github-002", "multi_oauth@souklab.com");
        persistIdentity(otherUser, "google", "sub-google-003", "other_oauth@souklab.com");

        entityManager.flush();
        entityManager.clear();

        List<OAuthIdentity> userIdentities = oAuthIdentityRepository.findByUser(userWithTwo);
        assertThat(userIdentities).hasSize(2);
        assertThat(userIdentities)
                .extracting(OAuthIdentity::getId)
                .containsExactlyInAnyOrder(googleIdentity.getId(), githubIdentity.getId());

        List<OAuthIdentity> emptyIdentities = oAuthIdentityRepository.findByUser(userWithNone);
        assertThat(emptyIdentities).isEmpty();
    }

    /**
     * Verifies findByProviderAndEmail returns the identity matching both provider and email,
     * and empty Optional when either does not match.
     */
    @Test
    @DisplayName("findByProviderAndEmail: returns identity matching provider and email, empty Optional when absent")
    void findByProviderAndEmail_verifiesFoundAndNotFound() {
        User user = persistUser("email_oauth@souklab.com");
        OAuthIdentity identity = persistIdentity(user, "google", "sub-email-777", "email_oauth@souklab.com");

        entityManager.flush();
        entityManager.clear();

        Optional<OAuthIdentity> found = oAuthIdentityRepository.findByProviderAndEmail("google", "email_oauth@souklab.com");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(identity.getId());
        assertThat(found.get().getProviderUserId()).isEqualTo("sub-email-777");

        Optional<OAuthIdentity> wrongProvider = oAuthIdentityRepository.findByProviderAndEmail("facebook", "email_oauth@souklab.com");
        assertThat(wrongProvider).isEmpty();

        Optional<OAuthIdentity> wrongEmail = oAuthIdentityRepository.findByProviderAndEmail("google", "nonexistent@souklab.com");
        assertThat(wrongEmail).isEmpty();
    }
}
