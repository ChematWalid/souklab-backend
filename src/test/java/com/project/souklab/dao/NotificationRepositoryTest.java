package com.project.souklab.dao;


import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Notification;
import com.project.souklab.model.NotificationType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest slice verifying NotificationRepository query derivation, soft-delete filtering,
 * pagination, unread counts, and the bulk modifying markAllAsReadForUser query against H2.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class NotificationRepositoryTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 9, 5, 12, 0, 0);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

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
     * Helper to persist a notification with given parameters.
     */
    private Notification persistNotification(User user, NotificationType.Key type, String targetId, String message, boolean isRead) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setMessage(message);
        notification.setRead(isRead);
        return entityManager.persist(notification);
    }

    /**
     * Verifies that findByUserAndDeletedAtIsNullOrderByCreatedAtDesc returns non-deleted notifications
     * ordered from most recent to oldest, and excludes other users and soft-deleted records.
     */
    @Test
    @DisplayName("findByUserAndDeletedAtIsNullOrderByCreatedAtDesc: orders by createdAt DESC and excludes soft-deleted and other users")
    void findByUserAndDeletedAtIsNullOrderByCreatedAtDesc_ordersByCreatedAtDescAndAppliesFilters() {
        User targetUser = persistUser("target@souklab.com");
        User otherUser = persistUser("other@souklab.com");

        Notification olderNotification = persistNotification(
                targetUser, NotificationType.Payment.SUCCESS, "tx-older", "Older payment notification", false);
        Notification newerNotification = persistNotification(
                targetUser, NotificationType.Payment.FAILED, "tx-newer", "Newer payment failure", false);
        Notification deletedNotification = persistNotification(
                targetUser, NotificationType.Account.VALIDATED, "val-1", "Deleted notification", false);
        deletedNotification.setDeletedAt(FIXED_NOW.minusHours(1));

        Notification otherUserNotification = persistNotification(
                otherUser, NotificationType.Payment.SUCCESS, "tx-other", "Other user notification", false);

        entityManager.flush();

        entityManager.getEntityManager().createQuery(
                "UPDATE Notification n SET n.createdAt = :ts WHERE n.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(3))
                .setParameter("id", olderNotification.getId())
                .executeUpdate();

        entityManager.getEntityManager().createQuery(
                "UPDATE Notification n SET n.createdAt = :ts WHERE n.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(1))
                .setParameter("id", newerNotification.getId())
                .executeUpdate();

        entityManager.clear();

        Page<Notification> page = notificationRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(
                targetUser, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(Notification::getId)
                .containsExactly(newerNotification.getId(), olderNotification.getId());
    }

    /**
     * Verifies pagination support on findByUserAndDeletedAtIsNullOrderByCreatedAtDesc.
     */
    @Test
    @DisplayName("findByUserAndDeletedAtIsNullOrderByCreatedAtDesc: applies page boundaries correctly")
    void findByUserAndDeletedAtIsNullOrderByCreatedAtDesc_appliesPagination() {
        User user = persistUser("paged@souklab.com");

        Notification notif1 = persistNotification(user, NotificationType.Payment.SUCCESS, "p1", "Msg 1", false);
        Notification notif2 = persistNotification(user, NotificationType.Payment.SUCCESS, "p2", "Msg 2", false);
        Notification notif3 = persistNotification(user, NotificationType.Payment.SUCCESS, "p3", "Msg 3", false);

        entityManager.flush();

        entityManager.getEntityManager().createQuery(
                "UPDATE Notification n SET n.createdAt = :ts WHERE n.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(3))
                .setParameter("id", notif1.getId())
                .executeUpdate();

        entityManager.getEntityManager().createQuery(
                "UPDATE Notification n SET n.createdAt = :ts WHERE n.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(2))
                .setParameter("id", notif2.getId())
                .executeUpdate();

        entityManager.getEntityManager().createQuery(
                "UPDATE Notification n SET n.createdAt = :ts WHERE n.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(1))
                .setParameter("id", notif3.getId())
                .executeUpdate();

        entityManager.clear();

        Page<Notification> firstPage = notificationRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(
                user, PageRequest.of(0, 2));
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getContent().get(0).getId()).isEqualTo(notif3.getId());
        assertThat(firstPage.getContent().get(1).getId()).isEqualTo(notif2.getId());

        Page<Notification> secondPage = notificationRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(
                user, PageRequest.of(1, 2));
        assertThat(secondPage.getTotalElements()).isEqualTo(3);
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getContent().get(0).getId()).isEqualTo(notif1.getId());
    }

    /**
     * Verifies that findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc
     * returns the single latest matching notification.
     */
    @Test
    @DisplayName("findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc: returns latest match")
    void findFirstByUserAndTypeAndTargetId_returnsLatestNonDeletedNotification() {
        User user = persistUser("latest@souklab.com");

        Notification older = persistNotification(
                user, NotificationType.Message.NEW, "msg-conv-1", "Older chat message", false);
        Notification newer = persistNotification(
                user, NotificationType.Message.NEW, "msg-conv-1", "Newer chat message", false);

        entityManager.flush();

        entityManager.getEntityManager().createQuery(
                "UPDATE Notification n SET n.createdAt = :ts WHERE n.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(2))
                .setParameter("id", older.getId())
                .executeUpdate();

        entityManager.getEntityManager().createQuery(
                "UPDATE Notification n SET n.createdAt = :ts WHERE n.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(1))
                .setParameter("id", newer.getId())
                .executeUpdate();

        entityManager.clear();

        Optional<Notification> result = notificationRepository
                .findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        user, NotificationType.Message.NEW, "msg-conv-1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(newer.getId());
        assertThat(result.get().getMessage()).isEqualTo("Newer chat message");
    }

    /**
     * Verifies that findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc
     * returns empty when matching notification is soft-deleted or when criteria differ.
     */
    @Test
    @DisplayName("findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc: returns empty when deleted or criteria mismatch")
    void findFirstByUserAndTypeAndTargetId_whenDeletedOrNoMatch_returnsEmpty() {
        User user = persistUser("mismatch@souklab.com");

        Notification deleted = persistNotification(
                user, NotificationType.Message.NEW, "conv-99", "Deleted message", false);
        deleted.setDeletedAt(FIXED_NOW);
        entityManager.persist(deleted);
        entityManager.flush();
        entityManager.clear();

        Optional<Notification> deletedMatch = notificationRepository
                .findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        user, NotificationType.Message.NEW, "conv-99");
        assertThat(deletedMatch).isEmpty();

        Optional<Notification> wrongType = notificationRepository
                .findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        user, NotificationType.Payment.SUCCESS, "conv-99");
        assertThat(wrongType).isEmpty();

        Optional<Notification> wrongTarget = notificationRepository
                .findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        user, NotificationType.Message.NEW, "nonexistent-target");
        assertThat(wrongTarget).isEmpty();
    }

    /**
     * Verifies that findByIdAndUserAndDeletedAtIsNull returns the notification when owned by user
     * and not soft-deleted, but returns empty when belonging to another user, soft-deleted, or missing.
     */
    @Test
    @DisplayName("findByIdAndUserAndDeletedAtIsNull: enforces ownership and non-deleted check")
    void findByIdAndUserAndDeletedAtIsNull_enforcesOwnershipAndDeletedCheck() {
        User owner = persistUser("owner@souklab.com");
        User intruder = persistUser("intruder@souklab.com");

        Notification activeNotif = persistNotification(
                owner, NotificationType.Account.VALIDATED, "act-1", "Owner alert", false);
        Notification deletedNotif = persistNotification(
                owner, NotificationType.Account.VALIDATED, "act-2", "Owner deleted alert", false);
        deletedNotif.setDeletedAt(FIXED_NOW);

        entityManager.persist(deletedNotif);
        entityManager.flush();
        entityManager.clear();

        Optional<Notification> found = notificationRepository.findByIdAndUserAndDeletedAtIsNull(
                activeNotif.getId(), owner);
        assertThat(found).isPresent();
        assertThat(found.get().getMessage()).isEqualTo("Owner alert");

        Optional<Notification> wrongOwner = notificationRepository.findByIdAndUserAndDeletedAtIsNull(
                activeNotif.getId(), intruder);
        assertThat(wrongOwner).isEmpty();

        Optional<Notification> softDeleted = notificationRepository.findByIdAndUserAndDeletedAtIsNull(
                deletedNotif.getId(), owner);
        assertThat(softDeleted).isEmpty();

        Optional<Notification> nonExistent = notificationRepository.findByIdAndUserAndDeletedAtIsNull(
                "non-existent-id", owner);
        assertThat(nonExistent).isEmpty();
    }

    /**
     * Verifies that countByUserAndIsReadFalseAndDeletedAtIsNull counts only unread, non-deleted
     * notifications for the specified user, ignoring read, soft-deleted, and other users' records.
     */
    @Test
    @DisplayName("countByUserAndIsReadFalseAndDeletedAtIsNull: counts only unread non-deleted for user")
    void countByUserAndIsReadFalseAndDeletedAtIsNull_countsOnlyUnreadNonDeletedForTargetUser() {
        User userA = persistUser("usera@souklab.com");
        User userB = persistUser("userb@souklab.com");

        persistNotification(userA, NotificationType.Message.NEW, "m1", "Unread 1", false);
        persistNotification(userA, NotificationType.Message.NEW, "m2", "Unread 2", false);
        persistNotification(userA, NotificationType.Message.NEW, "m3", "Already read", true);

        Notification deletedUnreadA = persistNotification(userA, NotificationType.Message.NEW, "m4", "Deleted unread", false);
        deletedUnreadA.setDeletedAt(FIXED_NOW);
        entityManager.persist(deletedUnreadA);

        persistNotification(userB, NotificationType.Message.NEW, "m5", "User B unread", false);
        persistNotification(userB, NotificationType.Message.NEW, "m6", "User B unread 2", false);

        entityManager.flush();
        entityManager.clear();

        long countA = notificationRepository.countByUserAndIsReadFalseAndDeletedAtIsNull(userA);
        assertThat(countA).isEqualTo(2);

        long countB = notificationRepository.countByUserAndIsReadFalseAndDeletedAtIsNull(userB);
        assertThat(countB).isEqualTo(2);
    }

    /**
     * Verifies that markAllAsReadForUser updates only unread, non-deleted notifications for the target
     * user, updates updatedAt, leaves already-read notifications untouched, leaves other users untouched,
     * and returns the exact number of modified rows.
     */
    @Test
    @DisplayName("markAllAsReadForUser: bulk updates unread notifications for target user and returns affected count")
    void markAllAsReadForUser_updatesOnlyUnreadNonDeletedForTargetUser() {
        User targetUser = persistUser("target_mark@souklab.com");
        User otherUser = persistUser("other_mark@souklab.com");

        Notification targetUnread1 = persistNotification(
                targetUser, NotificationType.Payment.SUCCESS, "p1", "Unread 1", false);
        Notification targetUnread2 = persistNotification(
                targetUser, NotificationType.Payment.FAILED, "p2", "Unread 2", false);
        Notification targetAlreadyRead = persistNotification(
                targetUser, NotificationType.Account.VALIDATED, "a1", "Already read", true);

        Notification targetDeletedUnread = persistNotification(
                targetUser, NotificationType.Account.SUSPENDED, "a2", "Deleted unread", false);
        targetDeletedUnread.setDeletedAt(FIXED_NOW);
        entityManager.persist(targetDeletedUnread);

        Notification otherUnread = persistNotification(
                otherUser, NotificationType.Payment.SUCCESS, "p3", "Other unread", false);

        entityManager.flush();
        entityManager.clear();

        int updatedCount = notificationRepository.markAllAsReadForUser(targetUser);
        assertThat(updatedCount).isEqualTo(2);

        entityManager.clear();

        Notification reloadedUnread1 = notificationRepository.findById(targetUnread1.getId()).orElseThrow();
        assertThat(reloadedUnread1.isRead()).isTrue();
        assertThat(reloadedUnread1.getUpdatedAt()).isNotNull();

        Notification reloadedUnread2 = notificationRepository.findById(targetUnread2.getId()).orElseThrow();
        assertThat(reloadedUnread2.isRead()).isTrue();
        assertThat(reloadedUnread2.getUpdatedAt()).isNotNull();

        Notification reloadedAlreadyRead = notificationRepository.findById(targetAlreadyRead.getId()).orElseThrow();
        assertThat(reloadedAlreadyRead.isRead()).isTrue();

        Notification reloadedDeleted = notificationRepository.findById(targetDeletedUnread.getId()).orElseThrow();
        assertThat(reloadedDeleted.isRead()).isFalse();

        Notification reloadedOther = notificationRepository.findById(otherUnread.getId()).orElseThrow();
        assertThat(reloadedOther.isRead()).isFalse();
    }
}
