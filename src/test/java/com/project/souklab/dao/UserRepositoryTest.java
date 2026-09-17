package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest slice verifying UserRepository query derivation, custom JPQL queries,
 * permission join filtering with soft-delete exclusion, and eager EntityGraph permission loading in H2.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.search.enabled=false"
})
class UserRepositoryTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 9, 5, 12, 0, 0);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * Verifies that findByEmail returns the user and that the @EntityGraph eagerly loads
     * associated permissions even after clearing the persistence context (detached entity state).
     */
    @Test
    @DisplayName("findByEmail: loads user and eagerly fetches permissions after persistence context is cleared")
    void findByEmail_whenUserExists_returnsUserWithRolesEagerlyLoadedAfterContextCleared() {
        AuthorizationPermission roleClient = new AuthorizationPermission();
        roleClient.setPermissionKey("permission:profile:read");
        roleClient.setDescription("Read profiles");
        entityManager.persist(roleClient);

        AuthorizationPermission roleArtisan = new AuthorizationPermission();
        roleArtisan.setPermissionKey("permission:artisan:content");
        roleArtisan.setDescription("Create artisan content");
        entityManager.persist(roleArtisan);

        User user = User.builder()
                .email("artisan@souklab.com")
                .firstName("Karim")
                .lastName("Mansouri")
                .status(AccountStatus.ACTIVE)
                .permissions(Set.of(roleClient, roleArtisan))
                .build();
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByEmail("artisan@souklab.com");

        assertThat(found).isPresent();
        User detachedUser = found.get();
        assertThat(detachedUser.getEmail()).isEqualTo("artisan@souklab.com");
        assertThat(detachedUser.getPermissions())
                .hasSize(2)
                .extracting(AuthorizationPermission::getPermissionKey)
                .containsExactlyInAnyOrder("permission:profile:read", "permission:artisan:content");
    }

    /**
     * Verifies that findByEmail returns an empty Optional when no user matches the email address.
     */
    @Test
    @DisplayName("findByEmail: returns empty when email does not exist")
    void findByEmail_whenUserDoesNotExist_returnsEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@souklab.com");

        assertThat(found).isEmpty();
    }

    /**
     * Verifies that existsByEmail returns true for existing emails and false otherwise.
     */
    @Test
    @DisplayName("existsByEmail: returns true for existing emails and false for missing ones")
    void existsByEmail_returnsTrueWhenEmailExists_andFalseWhenMissing() {
        User user = User.builder()
                .email("present@souklab.com")
                .status(AccountStatus.ACTIVE)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.existsByEmail("present@souklab.com")).isTrue();
        assertThat(userRepository.existsByEmail("absent@souklab.com")).isFalse();
    }

    /**
     * Verifies that findByStatus filters strictly by AccountStatus and applies pagination,
     * excluding users with different statuses.
     */
    @Test
    @DisplayName("findByStatus: filters by AccountStatus and applies pagination excluding other statuses")
    void findByStatus_filtersByStatusAndAppliesPaginationExcludingOtherStatuses() {
        User active1 = User.builder().email("active1@souklab.com").status(AccountStatus.ACTIVE).build();
        User active2 = User.builder().email("active2@souklab.com").status(AccountStatus.ACTIVE).build();
        User pending = User.builder().email("pending@souklab.com").status(AccountStatus.PENDING).build();
        User suspended = User.builder().email("suspended@souklab.com").status(AccountStatus.SUSPENDED).build();

        entityManager.persist(active1);
        entityManager.persist(active2);
        entityManager.persist(pending);
        entityManager.persist(suspended);
        entityManager.flush();
        entityManager.clear();

        Page<User> activePage = userRepository.findByStatus(AccountStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(activePage.getTotalElements()).isEqualTo(2);
        assertThat(activePage.getContent())
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("active1@souklab.com", "active2@souklab.com");

        Page<User> activePagePaged = userRepository.findByStatus(AccountStatus.ACTIVE, PageRequest.of(0, 1));
        assertThat(activePagePaged.getTotalElements()).isEqualTo(2);
        assertThat(activePagePaged.getContent()).hasSize(1);
    }

    /**
     * Verifies that default method existsByUsername correctly delegates to existsByEmail.
     */
    @Test
    @DisplayName("existsByUsername: delegates to existsByEmail matching on user email")
    void existsByUsername_delegatesToExistsByEmail() {
        User user = User.builder()
                .email("delegation@souklab.com")
                .status(AccountStatus.ACTIVE)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.existsByUsername("delegation@souklab.com")).isTrue();
        assertThat(userRepository.existsByUsername("unknown@souklab.com")).isFalse();
    }

    /**
     * Verifies that findByUsername matches by email and eagerly loads permissions via EntityGraph.
     */
    @Test
    @DisplayName("findByUsername: matches by email and eagerly loads permissions")
    void findByUsername_matchesByEmailAndEagerlyLoadsRoles() {
        AuthorizationPermission role = new AuthorizationPermission();
        role.setPermissionKey("permission:admin:users");
        role.setDescription("Manage users");
        entityManager.persist(role);

        User user = User.builder()
                .email("admin@souklab.com")
                .status(AccountStatus.ACTIVE)
                .permissions(Set.of(role))
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByUsername("admin@souklab.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("admin@souklab.com");
        assertThat(found.get().getPermissions())
                .hasSize(1)
                .extracting(AuthorizationPermission::getPermissionKey)
                .containsExactly("permission:admin:users");
    }

    /**
     * Verifies that findByUsername returns empty when the query does not match any user's email.
     */
    @Test
    @DisplayName("findByUsername: returns empty when email does not match")
    void findByUsername_whenNotMatchingEmail_returnsEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent@souklab.com");

        assertThat(found).isEmpty();
    }

    /**
     * Verifies that searchUsers matches case-insensitive substrings against email.
     */
    @Test
    @DisplayName("searchUsers: matches case-insensitive substring on email")
    void searchUsers_matchesCaseInsensitiveSubstringOnEmail() {
        User target = User.builder()
                .email("abdelkarim.artisan@souklab.com")
                .firstName("Karim")
                .lastName("Mansouri")
                .status(AccountStatus.ACTIVE)
                .build();
        User other = User.builder()
                .email("sofiane@souklab.com")
                .firstName("Sofiane")
                .lastName("Kaci")
                .status(AccountStatus.ACTIVE)
                .build();

        entityManager.persist(target);
        entityManager.persist(other);
        entityManager.flush();
        entityManager.clear();

        Page<User> result = userRepository.searchUsers("KaRiM.aRtIsAn", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("abdelkarim.artisan@souklab.com");
    }

    /**
     * Verifies that searchUsers matches case-insensitive substrings against firstName.
     */
    @Test
    @DisplayName("searchUsers: matches case-insensitive substring on firstName")
    void searchUsers_matchesCaseInsensitiveSubstringOnFirstName() {
        User target = User.builder()
                .email("user1@souklab.com")
                .firstName("Mohamed")
                .lastName("Brahimi")
                .status(AccountStatus.ACTIVE)
                .build();
        User other = User.builder()
                .email("user2@souklab.com")
                .firstName("Yacine")
                .lastName("Dahmani")
                .status(AccountStatus.ACTIVE)
                .build();

        entityManager.persist(target);
        entityManager.persist(other);
        entityManager.flush();
        entityManager.clear();

        Page<User> result = userRepository.searchUsers("hAmE", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Mohamed");
    }

    /**
     * Verifies that searchUsers matches case-insensitive substrings against lastName.
     */
    @Test
    @DisplayName("searchUsers: matches case-insensitive substring on lastName")
    void searchUsers_matchesCaseInsensitiveSubstringOnLastName() {
        User target = User.builder()
                .email("user3@souklab.com")
                .firstName("Amine")
                .lastName("Benali")
                .status(AccountStatus.ACTIVE)
                .build();
        User other = User.builder()
                .email("user4@souklab.com")
                .firstName("Farid")
                .lastName("Ziani")
                .status(AccountStatus.ACTIVE)
                .build();

        entityManager.persist(target);
        entityManager.persist(other);
        entityManager.flush();
        entityManager.clear();

        Page<User> result = userRepository.searchUsers("eNaLi", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("Benali");
    }

    /**
     * Verifies that searchUsers returns an empty page when query matches none of the fields.
     */
    @Test
    @DisplayName("searchUsers: returns empty page when query matches none of the fields")
    void searchUsers_whenNoFieldMatches_returnsEmptyPage() {
        User user = User.builder()
                .email("test@souklab.com")
                .firstName("TestFirst")
                .lastName("TestLast")
                .status(AccountStatus.ACTIVE)
                .build();

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        Page<User> result = userRepository.searchUsers("NonexistentQueryString999", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    /**
     * Verifies that searchUsers correctly handles page boundaries and total counts across multiple matches.
     */
    @Test
    @DisplayName("searchUsers: applies pagination correctly across multiple matching records")
    void searchUsers_appliesPaginationCorrectlyAcrossMultipleMatches() {
        User user1 = User.builder().email("search_match1@souklab.com").firstName("Walid").lastName("Un").build();
        User user2 = User.builder().email("search_match2@souklab.com").firstName("Walid").lastName("Deux").build();
        User user3 = User.builder().email("search_match3@souklab.com").firstName("Walid").lastName("Trois").build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
        entityManager.clear();

        Page<User> page0 = userRepository.searchUsers("Walid", PageRequest.of(0, 2));
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getContent()).hasSize(2);

        Page<User> page1 = userRepository.searchUsers("Walid", PageRequest.of(1, 2));
        assertThat(page1.getTotalElements()).isEqualTo(3);
        assertThat(page1.getContent()).hasSize(1);
    }

    /**
     * Verifies that findByPermissionKey returns active users with the requested role and strictly
     * excludes soft-deleted users (deletedAt IS NOT NULL) and users with other permissions.
     */
    @Test
    @DisplayName("findByPermissionKey: returns active users with role and excludes soft-deleted users")
    void findByPermissionKey_returnsActiveUsersWithRole_andExcludesSoftDeletedUsers() {
        AuthorizationPermission adminRole = new AuthorizationPermission();
        adminRole.setPermissionKey("permission:admin:users");
        adminRole.setDescription("Manage users");
        entityManager.persist(adminRole);

        AuthorizationPermission clientRole = new AuthorizationPermission();
        clientRole.setPermissionKey("permission:profile:read");
        clientRole.setDescription("Read profiles");
        entityManager.persist(clientRole);

        User activeAdmin = User.builder()
                .email("active.admin@souklab.com")
                .status(AccountStatus.ACTIVE)
                .permissions(Set.of(adminRole))
                .build();

        User softDeletedAdmin = User.builder()
                .email("deleted.admin@souklab.com")
                .status(AccountStatus.ACTIVE)
                .permissions(Set.of(adminRole))
                .build();
        softDeletedAdmin.setDeletedAt(FIXED_NOW.minusDays(1));

        User activeClient = User.builder()
                .email("active.client@souklab.com")
                .status(AccountStatus.ACTIVE)
                .permissions(Set.of(clientRole))
                .build();

        entityManager.persist(activeAdmin);
        entityManager.persist(softDeletedAdmin);
        entityManager.persist(activeClient);
        entityManager.flush();
        entityManager.clear();

        List<User> admins = userRepository.findByPermissionKey("permission:admin:users");

        assertThat(admins)
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly("active.admin@souklab.com");
    }
}
