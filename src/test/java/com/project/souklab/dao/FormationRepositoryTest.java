package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationEnrollment;
import com.project.souklab.model.FormationStatus;
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
 * Slice test verifying {@link FormationRepository} query derivation, pagination,
 * author ownership constraints, and soft-delete exclusions.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FormationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FormationRepository formationRepository;

    /**
     * Persists an active test user.
     */
    private User persistUser(String email) {
        User user = User.builder()
                .email(email)
                .firstName("Karim")
                .lastName("Artisan")
                .status(AccountStatus.ACTIVE)
                .build();
        return entityManager.persist(user);
    }

    /**
     * Persists an instructor artisan.
     */
    private Artisan persistArtisan(User user, boolean isTeacher) {
        Artisan artisan = Artisan.builder()
                .user(user)
                .city("Algiers")
                .isTeacher(isTeacher)
                .build();
        return entityManager.persist(artisan);
    }

    /**
     * Persists a formation entity with required attributes.
     */
    private Formation persistFormation(Artisan author, String title, FormationStatus status) {
        Formation formation = Formation.builder()
                .author(author)
                .title(title)
                .description("In-depth traditional ceramics masterclass")
                .status(status)
                .price(15000)
                .currency("DZD")
                .maxParticipants(12)
                .durationHours(6)
                .isOnline(false)
                .scheduledAt(LocalDateTime.now().plusDays(7))
                .build();
        return entityManager.persist(formation);
    }

    /**
     * Verifies findByStatusAndDeletedAtIsNull correctly filters by status and ignores soft-deleted formations.
     */
    @Test
    @DisplayName("findByStatusAndDeletedAtIsNull: returns active formations matching status and excludes soft-deleted")
    void findByStatusAndDeletedAtIsNull_returnsMatchingStatusAndExcludesSoftDeleted() {
        User user = persistUser("instructor1@souklab.dz");
        Artisan author = persistArtisan(user, true);

        Formation published1 = persistFormation(author, "Woodworking 101", FormationStatus.PUBLISHED);
        Formation published2 = persistFormation(author, "Leather Crafting", FormationStatus.PUBLISHED);
        Formation draft = persistFormation(author, "Draft Pottery", FormationStatus.DRAFT);
        Formation deletedPublished = persistFormation(author, "Deleted Pottery", FormationStatus.PUBLISHED);
        deletedPublished.setDeletedAt(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        Page<Formation> result = formationRepository.findByStatusAndDeletedAtIsNull(
                FormationStatus.PUBLISHED, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Formation::getId)
                .containsExactlyInAnyOrder(published1.getId(), published2.getId())
                .doesNotContain(draft.getId(), deletedPublished.getId());
    }

    /**
     * Verifies findByAuthorIdAndDeletedAtIsNull returns all active formations of a specific author.
     */
    @Test
    @DisplayName("findByAuthorIdAndDeletedAtIsNull: returns author formations and excludes soft-deleted")
    void findByAuthorIdAndDeletedAtIsNull_returnsAuthorFormations() {
        User user1 = persistUser("instructor_a@souklab.dz");
        User user2 = persistUser("instructor_b@souklab.dz");
        Artisan authorA = persistArtisan(user1, true);
        Artisan authorB = persistArtisan(user2, true);

        Formation f1 = persistFormation(authorA, "Copper Engraving A1", FormationStatus.PUBLISHED);
        Formation f2 = persistFormation(authorA, "Copper Engraving A2", FormationStatus.DRAFT);
        Formation f3 = persistFormation(authorB, "Carpet Weaving B1", FormationStatus.PUBLISHED);
        Formation deletedF = persistFormation(authorA, "Deleted Copper Course", FormationStatus.PUBLISHED);
        deletedF.setDeletedAt(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        Page<Formation> page = formationRepository.findByAuthorIdAndDeletedAtIsNull(
                authorA.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(Formation::getId)
                .containsExactlyInAnyOrder(f1.getId(), f2.getId())
                .doesNotContain(f3.getId(), deletedF.getId());
    }

    /**
     * Verifies findByIdAndAuthorIdAndDeletedAtIsNull ensures author ownership and excludes soft-deleted entities.
     */
    @Test
    @DisplayName("findByIdAndAuthorIdAndDeletedAtIsNull: returns formation only if author matches and not soft-deleted")
    void findByIdAndAuthorIdAndDeletedAtIsNull_verifiesAuthorOwnership() {
        User user1 = persistUser("owner@souklab.dz");
        User user2 = persistUser("other@souklab.dz");
        Artisan owner = persistArtisan(user1, true);
        Artisan other = persistArtisan(user2, true);

        Formation formation = persistFormation(owner, "Silversmithing Masterclass", FormationStatus.PUBLISHED);

        entityManager.flush();
        entityManager.clear();

        Optional<Formation> found = formationRepository.findByIdAndAuthorIdAndDeletedAtIsNull(formation.getId(), owner.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Silversmithing Masterclass");

        Optional<Formation> wrongAuthor = formationRepository.findByIdAndAuthorIdAndDeletedAtIsNull(formation.getId(), other.getId());
        assertThat(wrongAuthor).isEmpty();

        formation.setDeletedAt(LocalDateTime.now());
        entityManager.merge(formation);
        entityManager.flush();
        entityManager.clear();

        Optional<Formation> deletedResult = formationRepository.findByIdAndAuthorIdAndDeletedAtIsNull(formation.getId(), owner.getId());
        assertThat(deletedResult).isEmpty();
    }

    /**
     * Verifies findByIdAndDeletedAtIsNull retrieves active entity and returns empty for soft-deleted entity.
     */
    @Test
    @DisplayName("findByIdAndDeletedAtIsNull: returns active formation and empty for soft-deleted")
    void findByIdAndDeletedAtIsNull_verifiesActiveAndSoftDeleted() {
        User user = persistUser("tester@souklab.dz");
        Artisan author = persistArtisan(user, true);

        Formation active = persistFormation(author, "Pottery Essentials", FormationStatus.PUBLISHED);
        Formation deleted = persistFormation(author, "Discontinued Pottery", FormationStatus.PUBLISHED);
        deleted.setDeletedAt(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        assertThat(formationRepository.findByIdAndDeletedAtIsNull(active.getId())).isPresent();
        assertThat(formationRepository.findByIdAndDeletedAtIsNull(deleted.getId())).isEmpty();
        assertThat(formationRepository.findByIdAndDeletedAtIsNull("non-existent")).isEmpty();
    }

    /**
     * Verifies getActiveEnrollmentsCount calculates confirmed enrollments excluding cancelled or deleted ones.
     */
    @Test
    @DisplayName("getActiveEnrollmentsCount: correctly counts confirmed non-deleted enrollments")
    void getActiveEnrollmentsCount_calculatesConfirmedEnrollments() {
        User instructorUser = persistUser("teacher@souklab.dz");
        Artisan instructor = persistArtisan(instructorUser, true);
        Formation formation = persistFormation(instructor, "Embroidery Techniques", FormationStatus.PUBLISHED);

        User student1 = persistUser("student1@souklab.dz");
        User student2 = persistUser("student2@souklab.dz");
        User student3 = persistUser("student3@souklab.dz");
        Artisan artisan1 = persistArtisan(student1, false);
        Artisan artisan2 = persistArtisan(student2, false);
        Artisan artisan3 = persistArtisan(student3, false);

        FormationEnrollment e1 = FormationEnrollment.builder()
                .formation(formation)
                .artisan(artisan1)
                .status(EnrollmentStatus.CONFIRMED)
                .enrolledAt(LocalDateTime.now())
                .build();
        entityManager.persist(e1);
        formation.addEnrollment(e1);

        FormationEnrollment e2 = FormationEnrollment.builder()
                .formation(formation)
                .artisan(artisan2)
                .status(EnrollmentStatus.CANCELLED)
                .enrolledAt(LocalDateTime.now().minusDays(1))
                .cancelledAt(LocalDateTime.now())
                .build();
        entityManager.persist(e2);
        formation.addEnrollment(e2);

        FormationEnrollment e3 = FormationEnrollment.builder()
                .formation(formation)
                .artisan(artisan3)
                .status(EnrollmentStatus.CONFIRMED)
                .enrolledAt(LocalDateTime.now())
                .build();
        e3.setDeletedAt(LocalDateTime.now());
        entityManager.persist(e3);
        formation.addEnrollment(e3);

        assertThat(formation.getActiveEnrollmentsCount()).isEqualTo(1L);
    }
}
