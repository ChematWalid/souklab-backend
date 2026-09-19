package com.project.souklab.dao;


import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanFormateurRequest;
import com.project.souklab.model.FormateurRequestStatus;
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
 * DataJpaTest slice verifying ArtisanFormateurRequestRepository query derivation, status filtering,
 * soft-delete exclusion, latest-request lookup, existence checks, and pagination in H2.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ArtisanFormateurRequestRepositoryTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 9, 5, 12, 0, 0);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArtisanFormateurRequestRepository requestRepository;

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
     * Persists an artisan sharing its primary key with the given user via @MapsId.
     */
    private Artisan persistArtisan(User user) {
        Artisan artisan = Artisan.builder()
                .user(user)
                .city("Algiers")
                .build();
        return entityManager.persist(artisan);
    }

    /**
     * Persists an ArtisanFormateurRequest.
     */
    private ArtisanFormateurRequest persistRequest(Artisan artisan, FormateurRequestStatus status, String motivation) {
        ArtisanFormateurRequest request = ArtisanFormateurRequest.builder()
                .artisan(artisan)
                .status(status)
                .motivation(motivation)
                .canReapply(true)
                .build();
        return entityManager.persist(request);
    }

    /**
     * Verifies findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc filters by status, excludes soft-deleted
     * records, orders by createdAt descending, and correctly applies pagination.
     */
    @Test
    @DisplayName("findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc: filters status, excludes soft-deleted, and orders by createdAt DESC")
    void findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc_filtersStatusAndOrdersCorrectly() {
        User user1 = persistUser("artisan1@souklab.com");
        User user2 = persistUser("artisan2@souklab.com");
        User user3 = persistUser("artisan3@souklab.com");
        User user4 = persistUser("artisan4@souklab.com");

        Artisan artisan1 = persistArtisan(user1);
        Artisan artisan2 = persistArtisan(user2);
        Artisan artisan3 = persistArtisan(user3);
        Artisan artisan4 = persistArtisan(user4);

        ArtisanFormateurRequest olderPending = persistRequest(artisan1, FormateurRequestStatus.PENDING, "Older pending motivation");
        ArtisanFormateurRequest newerPending = persistRequest(artisan2, FormateurRequestStatus.PENDING, "Newer pending motivation");
        ArtisanFormateurRequest approvedReq = persistRequest(artisan3, FormateurRequestStatus.APPROVED, "Approved motivation");
        ArtisanFormateurRequest deletedPending = persistRequest(artisan4, FormateurRequestStatus.PENDING, "Deleted pending motivation");
        deletedPending.setDeletedAt(FIXED_NOW.minusHours(1));

        entityManager.flush();

        entityManager.getEntityManager().createQuery(
                "UPDATE ArtisanFormateurRequest r SET r.createdAt = :ts WHERE r.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(3))
                .setParameter("id", olderPending.getId())
                .executeUpdate();

        entityManager.getEntityManager().createQuery(
                "UPDATE ArtisanFormateurRequest r SET r.createdAt = :ts WHERE r.id = :id")
                .setParameter("ts", FIXED_NOW.minusHours(1))
                .setParameter("id", newerPending.getId())
                .executeUpdate();

        entityManager.clear();

        Page<ArtisanFormateurRequest> page = requestRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
                FormateurRequestStatus.PENDING, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(ArtisanFormateurRequest::getId)
                .containsExactly(newerPending.getId(), olderPending.getId());

        Page<ArtisanFormateurRequest> paged = requestRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
                FormateurRequestStatus.PENDING, PageRequest.of(0, 1));
        assertThat(paged.getTotalElements()).isEqualTo(2);
        assertThat(paged.getContent()).hasSize(1);
        assertThat(paged.getContent().get(0).getId()).isEqualTo(newerPending.getId());
    }

    /**
     * Verifies findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc returns the single latest
     * non-deleted request for the specified artisan when multiple requests exist.
     */
    @Test
    @DisplayName("findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc: returns latest non-deleted request")
    void findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc_returnsLatestNonDeleted() {
        User user = persistUser("single_artisan@souklab.com");
        Artisan artisan = persistArtisan(user);

        ArtisanFormateurRequest older = persistRequest(artisan, FormateurRequestStatus.REJECTED, "First attempt");
        ArtisanFormateurRequest newer = persistRequest(artisan, FormateurRequestStatus.PENDING, "Second attempt");

        entityManager.flush();

        entityManager.getEntityManager().createQuery(
                "UPDATE ArtisanFormateurRequest r SET r.createdAt = :ts WHERE r.id = :id")
                .setParameter("ts", FIXED_NOW.minusDays(5))
                .setParameter("id", older.getId())
                .executeUpdate();

        entityManager.getEntityManager().createQuery(
                "UPDATE ArtisanFormateurRequest r SET r.createdAt = :ts WHERE r.id = :id")
                .setParameter("ts", FIXED_NOW.minusDays(1))
                .setParameter("id", newer.getId())
                .executeUpdate();

        entityManager.clear();

        Optional<ArtisanFormateurRequest> result = requestRepository
                .findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc(artisan);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(newer.getId());
        assertThat(result.get().getMotivation()).isEqualTo("Second attempt");
    }

    /**
     * Verifies findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc returns empty when all matching
     * requests are soft-deleted or when the artisan has no requests.
     */
    @Test
    @DisplayName("findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc: returns empty when soft-deleted or absent")
    void findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc_whenSoftDeletedOrAbsent_returnsEmpty() {
        User userWithDeleted = persistUser("deleted_req@souklab.com");
        Artisan artisanWithDeleted = persistArtisan(userWithDeleted);
        ArtisanFormateurRequest deleted = persistRequest(artisanWithDeleted, FormateurRequestStatus.PENDING, "Deleted req");
        deleted.setDeletedAt(FIXED_NOW);

        User userWithoutReq = persistUser("no_req@souklab.com");
        Artisan artisanWithoutReq = persistArtisan(userWithoutReq);

        entityManager.flush();
        entityManager.clear();

        Optional<ArtisanFormateurRequest> deletedResult = requestRepository
                .findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc(artisanWithDeleted);
        assertThat(deletedResult).isEmpty();

        Optional<ArtisanFormateurRequest> absentResult = requestRepository
                .findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc(artisanWithoutReq);
        assertThat(absentResult).isEmpty();
    }

    /**
     * Verifies existsByArtisanAndStatusAndDeletedAtIsNull returns true for active matches, false
     * when status or artisan differs, and false when the request is soft-deleted.
     */
    @Test
    @DisplayName("existsByArtisanAndStatusAndDeletedAtIsNull: returns true for active matches, false when soft-deleted or absent")
    void existsByArtisanAndStatusAndDeletedAtIsNull_verifiesPresenceAndSoftDelete() {
        User user1 = persistUser("exists1@souklab.com");
        Artisan artisan1 = persistArtisan(user1);
        persistRequest(artisan1, FormateurRequestStatus.PENDING, "Active pending request");

        User user2 = persistUser("exists2@souklab.com");
        Artisan artisan2 = persistArtisan(user2);
        ArtisanFormateurRequest softDeleted = persistRequest(artisan2, FormateurRequestStatus.PENDING, "Soft-deleted request");
        softDeleted.setDeletedAt(FIXED_NOW);

        User user3 = persistUser("exists3@souklab.com");
        Artisan artisan3 = persistArtisan(user3);

        entityManager.flush();
        entityManager.clear();

        assertThat(requestRepository.existsByArtisanAndStatusAndDeletedAtIsNull(artisan1, FormateurRequestStatus.PENDING)).isTrue();
        assertThat(requestRepository.existsByArtisanAndStatusAndDeletedAtIsNull(artisan1, FormateurRequestStatus.APPROVED)).isFalse();
        assertThat(requestRepository.existsByArtisanAndStatusAndDeletedAtIsNull(artisan2, FormateurRequestStatus.PENDING)).isFalse();
        assertThat(requestRepository.existsByArtisanAndStatusAndDeletedAtIsNull(artisan3, FormateurRequestStatus.PENDING)).isFalse();
    }

    /**
     * Verifies findByIdAndDeletedAtIsNull returns the entity when active, but returns empty when
     * soft-deleted or non-existent.
     */
    @Test
    @DisplayName("findByIdAndDeletedAtIsNull: returns entity when not deleted, empty when soft-deleted or non-existent")
    void findByIdAndDeletedAtIsNull_verifiesActiveAndDeleted() {
        User user = persistUser("find_by_id@souklab.com");
        Artisan artisan = persistArtisan(user);

        ArtisanFormateurRequest active = persistRequest(artisan, FormateurRequestStatus.PENDING, "Active request");
        ArtisanFormateurRequest deleted = persistRequest(artisan, FormateurRequestStatus.REJECTED, "Deleted request");
        deleted.setDeletedAt(FIXED_NOW);

        entityManager.flush();
        entityManager.clear();

        Optional<ArtisanFormateurRequest> activeResult = requestRepository.findByIdAndDeletedAtIsNull(active.getId());
        assertThat(activeResult).isPresent();
        assertThat(activeResult.get().getMotivation()).isEqualTo("Active request");

        Optional<ArtisanFormateurRequest> deletedResult = requestRepository.findByIdAndDeletedAtIsNull(deleted.getId());
        assertThat(deletedResult).isEmpty();

        Optional<ArtisanFormateurRequest> absentResult = requestRepository.findByIdAndDeletedAtIsNull("non-existent-id");
        assertThat(absentResult).isEmpty();
    }
}
