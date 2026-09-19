package com.project.souklab.dao;


import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.User;
import com.project.souklab.model.UserAvatar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest slice verifying {@link UserAvatarRepository} query derivation,
 * ordering semantics, tenant isolation across multiple users, active avatar resolution,
 * and hard-delete behavior against embedded H2.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserAvatarRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserAvatarRepository userAvatarRepository;

    /**
     * Persists an active user for test scoping.
     *
     * @param email unique email of the user
     * @return persisted User entity
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
     * Persists a UserAvatar for the given user with specified active status and upload time.
     *
     * @param user owning user
     * @param originalFilename client original filename
     * @param isActive active avatar flag
     * @param uploadedAt upload timestamp
     * @return persisted UserAvatar entity
     */
    private UserAvatar persistAvatar(User user, String originalFilename, boolean isActive, LocalDateTime uploadedAt) {
        UserAvatar avatar = UserAvatar.builder()
                .user(user)
                .storageKeyOriginal("orig-" + originalFilename)
                .storageKeyMedium("med-" + originalFilename)
                .storageKeyThumbnail("thumb-" + originalFilename)
                .originalFilename(originalFilename)
                .contentType("image/jpeg")
                .fileSize(1024L)
                .isActive(isActive)
                .uploadedAt(uploadedAt)
                .build();
        return entityManager.persist(avatar);
    }

    /**
     * Verifies that findByUserIdOrderByUploadedAtDesc returns all avatars belonging to the user
     * in strict descending chronological order while excluding avatars belonging to other users.
     */
    @Test
    @DisplayName("findByUserIdOrderByUploadedAtDesc returns user avatars ordered newest to oldest and isolates by user")
    void findByUserIdOrderByUploadedAtDesc_returnsUserAvatarsInDescendingOrder() {
        User user1 = persistUser("user1@souklab.dz");
        User user2 = persistUser("user2@souklab.dz");

        LocalDateTime baseTime = LocalDateTime.of(2026, 9, 5, 10, 0, 0);
        UserAvatar avatarOlder = persistAvatar(user1, "older.jpg", false, baseTime.minusDays(2));
        UserAvatar avatarNewest = persistAvatar(user1, "newest.jpg", true, baseTime);
        UserAvatar avatarMiddle = persistAvatar(user1, "middle.jpg", false, baseTime.minusDays(1));

        persistAvatar(user2, "other-user.jpg", true, baseTime.plusHours(1));

        entityManager.flush();
        entityManager.clear();

        List<UserAvatar> results = userAvatarRepository.findByUserIdOrderByUploadedAtDesc(user1.getId());

        assertThat(results)
                .hasSize(3)
                .extracting(UserAvatar::getId)
                .containsExactly(avatarNewest.getId(), avatarMiddle.getId(), avatarOlder.getId());

        assertThat(results)
                .allSatisfy(avatar -> assertThat(avatar.getUser().getId()).isEqualTo(user1.getId()));
    }

    /**
     * Verifies that countByUserId counts only avatars for the target user.
     */
    @Test
    @DisplayName("countByUserId returns total count for the specified user and isolates from other users")
    void countByUserId_returnsAccurateCountPerUser() {
        User user1 = persistUser("user1@souklab.dz");
        User user2 = persistUser("user2@souklab.dz");

        assertThat(userAvatarRepository.countByUserId(user1.getId())).isZero();

        LocalDateTime now = LocalDateTime.of(2026, 9, 5, 11, 0, 0);
        persistAvatar(user1, "pic1.jpg", false, now.minusHours(2));
        persistAvatar(user1, "pic2.jpg", true, now.minusHours(1));
        persistAvatar(user2, "user2-pic.jpg", true, now);

        entityManager.flush();
        entityManager.clear();

        assertThat(userAvatarRepository.countByUserId(user1.getId())).isEqualTo(2);
        assertThat(userAvatarRepository.countByUserId(user2.getId())).isEqualTo(1);
    }

    /**
     * Verifies that findByIdAndUserId successfully finds an avatar when both ID and user ID match.
     */
    @Test
    @DisplayName("findByIdAndUserId returns avatar when owned by the specified user")
    void findByIdAndUserId_whenOwnedByUser_returnsAvatar() {
        User user = persistUser("owner@souklab.dz");
        UserAvatar avatar = persistAvatar(user, "avatar.jpg", true, LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        Optional<UserAvatar> found = userAvatarRepository.findByIdAndUserId(avatar.getId(), user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(avatar.getId());
        assertThat(found.get().getOriginalFilename()).isEqualTo("avatar.jpg");
    }

    /**
     * Verifies that findByIdAndUserId returns empty when the avatar exists but belongs to a different user.
     */
    @Test
    @DisplayName("findByIdAndUserId returns empty when avatar belongs to a different user")
    void findByIdAndUserId_whenNotOwnedByUser_returnsEmpty() {
        User owner = persistUser("owner@souklab.dz");
        User attacker = persistUser("attacker@souklab.dz");
        UserAvatar ownerAvatar = persistAvatar(owner, "owner-private.jpg", true, LocalDateTime.now());
        UserAvatar attackerAvatar = persistAvatar(attacker, "attacker-private.jpg", true, LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        Optional<UserAvatar> result = userAvatarRepository.findByIdAndUserId(ownerAvatar.getId(), attacker.getId());

        assertThat(result).isEmpty();
        assertThat(userAvatarRepository.findByIdAndUserId(attackerAvatar.getId(), owner.getId())).isEmpty();
    }

    /**
     * Verifies that findByIdAndUserId returns empty when the given avatar ID does not exist.
     */
    @Test
    @DisplayName("findByIdAndUserId returns empty when avatar ID does not exist")
    void findByIdAndUserId_whenNonexistentId_returnsEmpty() {
        User user = persistUser("user@souklab.dz");

        Optional<UserAvatar> result = userAvatarRepository.findByIdAndUserId("nonexistent-uuid", user.getId());

        assertThat(result).isEmpty();
    }

    /**
     * Verifies that findByUserIdAndIsActiveTrue returns the currently active avatar.
     */
    @Test
    @DisplayName("findByUserIdAndIsActiveTrue returns the active avatar for the user")
    void findByUserIdAndIsActiveTrue_whenActiveAvatarExists_returnsActiveAvatar() {
        User user = persistUser("user@souklab.dz");
        LocalDateTime now = LocalDateTime.of(2026, 9, 5, 11, 0, 0);

        persistAvatar(user, "inactive.jpg", false, now.minusDays(1));
        UserAvatar activeAvatar = persistAvatar(user, "active.jpg", true, now);

        entityManager.flush();
        entityManager.clear();

        Optional<UserAvatar> result = userAvatarRepository.findByUserIdAndIsActiveTrue(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeAvatar.getId());
        assertThat(result.get().isActive()).isTrue();
        assertThat(result.get().getOriginalFilename()).isEqualTo("active.jpg");
    }

    /**
     * Verifies that findByUserIdAndIsActiveTrue returns empty when the user has avatars but none is marked active.
     */
    @Test
    @DisplayName("findByUserIdAndIsActiveTrue returns empty when no avatar is active")
    void findByUserIdAndIsActiveTrue_whenNoActiveAvatar_returnsEmpty() {
        User user = persistUser("user@souklab.dz");
        persistAvatar(user, "inactive.jpg", false, LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        Optional<UserAvatar> result = userAvatarRepository.findByUserIdAndIsActiveTrue(user.getId());

        assertThat(result).isEmpty();
    }

    /**
     * Verifies that hard-deleting a UserAvatar physically removes the entity from the database.
     */
    @Test
    @DisplayName("delete physically removes the UserAvatar entity from the database")
    void delete_physicallyRemovesRecordFromDatabase() {
        User user = persistUser("user@souklab.dz");
        UserAvatar avatar = persistAvatar(user, "to-delete.jpg", false, LocalDateTime.now());

        entityManager.flush();

        userAvatarRepository.delete(avatar);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAvatar> foundById = userAvatarRepository.findById(avatar.getId());
        Optional<UserAvatar> foundByIdAndUser = userAvatarRepository.findByIdAndUserId(avatar.getId(), user.getId());
        UserAvatar foundInEm = entityManager.find(UserAvatar.class, avatar.getId());

        assertThat(foundById).isEmpty();
        assertThat(foundByIdAndUser).isEmpty();
        assertThat(foundInEm).isNull();
        assertThat(userAvatarRepository.countByUserId(user.getId())).isZero();
    }

    /**
     * Verifies that findByUserId with Pageable returns paginated avatars for the specified user
     * ordered according to the requested Sort specification while isolating from other users.
     */
    @Test
    @DisplayName("findByUserId with Pageable returns paged avatars for the specified user")
    void findByUserId_withPageable_returnsPagedResultsForUser() {
        User user1 = persistUser("user1@souklab.dz");
        User user2 = persistUser("user2@souklab.dz");

        LocalDateTime baseTime = LocalDateTime.of(2026, 9, 5, 10, 0, 0);
        UserAvatar avatar1 = persistAvatar(user1, "first.jpg", false, baseTime.minusDays(2));
        UserAvatar avatar2 = persistAvatar(user1, "second.jpg", true, baseTime);
        UserAvatar avatar3 = persistAvatar(user1, "third.jpg", false, baseTime.minusDays(1));
        persistAvatar(user2, "user2.jpg", true, baseTime);

        entityManager.flush();
        entityManager.clear();

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<UserAvatar> page = userAvatarRepository.findByUserId(user1.getId(), pageRequest);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getId()).isEqualTo(avatar2.getId());
        assertThat(page.getContent().get(1).getId()).isEqualTo(avatar3.getId());
    }
}
