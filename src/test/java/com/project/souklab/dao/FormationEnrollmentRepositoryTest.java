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
 * Slice test verifying {@link FormationEnrollmentRepository} queries, existence checks,
 * status filtering, counts, and artisan history pagination.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FormationEnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FormationEnrollmentRepository enrollmentRepository;

    /**
     * Persists an active test user.
     */
    private User persistUser(String email) {
        User user = User.builder()
                .email(email)
                .firstName("Test")
                .lastName("Artisan")
                .status(AccountStatus.ACTIVE)
                .build();
        return entityManager.persist(user);
    }

    /**
     * Persists an artisan.
     */
    private Artisan persistArtisan(User user, boolean isTeacher) {
        Artisan artisan = Artisan.builder()
                .user(user)
                .city("Tlemcen")
                .isTeacher(isTeacher)
                .build();
        return entityManager.persist(artisan);
    }

    /**
     * Persists a formation.
     */
    private Formation persistFormation(Artisan author, String title) {
        Formation formation = Formation.builder()
                .author(author)
                .title(title)
                .description("Masterclass description")
                .status(FormationStatus.PUBLISHED)
                .price(10000)
                .currency("DZD")
                .maxParticipants(10)
                .durationHours(4)
                .isOnline(true)
                .scheduledAt(LocalDateTime.now().plusDays(5))
                .build();
        return entityManager.persist(formation);
    }

    /**
     * Persists a formation enrollment record.
     */
    private FormationEnrollment persistEnrollment(Formation formation, Artisan artisan, EnrollmentStatus status) {
        FormationEnrollment enrollment = FormationEnrollment.builder()
                .formation(formation)
                .artisan(artisan)
                .status(status)
                .enrolledAt(LocalDateTime.now())
                .build();
        return entityManager.persist(enrollment);
    }

    /**
     * Verifies existsByFormationIdAndArtisanIdAndStatus correctly matches status and participants.
     */
    @Test
    @DisplayName("existsByFormationIdAndArtisanIdAndStatus: returns true when matching enrollment exists, false otherwise")
    void existsByFormationIdAndArtisanIdAndStatus_verifiesPresence() {
        User teacherUser = persistUser("teacher1@souklab.dz");
        Artisan teacher = persistArtisan(teacherUser, true);
        Formation formation = persistFormation(teacher, "Jewelry Making");

        User studentUser1 = persistUser("student_a@souklab.dz");
        User studentUser2 = persistUser("student_b@souklab.dz");
        Artisan studentA = persistArtisan(studentUser1, false);
        Artisan studentB = persistArtisan(studentUser2, false);

        persistEnrollment(formation, studentA, EnrollmentStatus.CONFIRMED);
        persistEnrollment(formation, studentB, EnrollmentStatus.CANCELLED);

        entityManager.flush();
        entityManager.clear();

        assertThat(enrollmentRepository.existsByFormationIdAndArtisanIdAndStatus(
                formation.getId(), studentA.getId(), EnrollmentStatus.CONFIRMED)).isTrue();
        assertThat(enrollmentRepository.existsByFormationIdAndArtisanIdAndStatus(
                formation.getId(), studentA.getId(), EnrollmentStatus.CANCELLED)).isFalse();
        assertThat(enrollmentRepository.existsByFormationIdAndArtisanIdAndStatus(
                formation.getId(), studentB.getId(), EnrollmentStatus.CONFIRMED)).isFalse();
        assertThat(enrollmentRepository.existsByFormationIdAndArtisanIdAndStatus(
                formation.getId(), studentB.getId(), EnrollmentStatus.CANCELLED)).isTrue();
    }

    /**
     * Verifies findByFormationIdAndArtisanIdAndStatus retrieves the specific enrollment record.
     */
    @Test
    @DisplayName("findByFormationIdAndArtisanIdAndStatus: returns matching enrollment optional")
    void findByFormationIdAndArtisanIdAndStatus_returnsMatchingOptional() {
        User teacherUser = persistUser("teacher2@souklab.dz");
        Artisan teacher = persistArtisan(teacherUser, true);
        Formation formation = persistFormation(teacher, "Glassblowing");

        User studentUser = persistUser("student_c@souklab.dz");
        Artisan student = persistArtisan(studentUser, false);

        FormationEnrollment enrollment = persistEnrollment(formation, student, EnrollmentStatus.CONFIRMED);

        entityManager.flush();
        entityManager.clear();

        Optional<FormationEnrollment> found = enrollmentRepository.findByFormationIdAndArtisanIdAndStatus(
                formation.getId(), student.getId(), EnrollmentStatus.CONFIRMED);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(enrollment.getId());

        Optional<FormationEnrollment> notFound = enrollmentRepository.findByFormationIdAndArtisanIdAndStatus(
                formation.getId(), student.getId(), EnrollmentStatus.CANCELLED);
        assertThat(notFound).isEmpty();
    }

    /**
     * Verifies findByFormationIdAndArtisanId finds the enrollment regardless of its current status,
     * supporting enrollment reactivation.
     */
    @Test
    @DisplayName("findByFormationIdAndArtisanId: returns enrollment regardless of status")
    void findByFormationIdAndArtisanId_returnsAnyStatus() {
        User teacherUser = persistUser("teacher3@souklab.dz");
        Artisan teacher = persistArtisan(teacherUser, true);
        Formation formation = persistFormation(teacher, "Mosaic Design");

        User studentUser = persistUser("student_d@souklab.dz");
        Artisan student = persistArtisan(studentUser, false);

        FormationEnrollment enrollment = persistEnrollment(formation, student, EnrollmentStatus.CANCELLED);

        entityManager.flush();
        entityManager.clear();

        Optional<FormationEnrollment> found = enrollmentRepository.findByFormationIdAndArtisanId(
                formation.getId(), student.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(enrollment.getId());
        assertThat(found.get().getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    /**
     * Verifies countByFormationIdAndStatus counts enrollments for a given status.
     */
    @Test
    @DisplayName("countByFormationIdAndStatus: accurately returns count of enrollments by status")
    void countByFormationIdAndStatus_countsMatching() {
        User teacherUser = persistUser("teacher4@souklab.dz");
        Artisan teacher = persistArtisan(teacherUser, true);
        Formation formation = persistFormation(teacher, "Ceramic Glazing");

        User u1 = persistUser("s1@souklab.dz");
        User u2 = persistUser("s2@souklab.dz");
        User u3 = persistUser("s3@souklab.dz");
        Artisan a1 = persistArtisan(u1, false);
        Artisan a2 = persistArtisan(u2, false);
        Artisan a3 = persistArtisan(u3, false);

        persistEnrollment(formation, a1, EnrollmentStatus.CONFIRMED);
        persistEnrollment(formation, a2, EnrollmentStatus.CONFIRMED);
        persistEnrollment(formation, a3, EnrollmentStatus.CANCELLED);

        entityManager.flush();
        entityManager.clear();

        assertThat(enrollmentRepository.countByFormationIdAndStatus(formation.getId(), EnrollmentStatus.CONFIRMED)).isEqualTo(2L);
        assertThat(enrollmentRepository.countByFormationIdAndStatus(formation.getId(), EnrollmentStatus.CANCELLED)).isEqualTo(1L);
        assertThat(enrollmentRepository.countByFormationIdAndStatus(formation.getId(), EnrollmentStatus.ATTENDED)).isZero();
    }

    /**
     * Verifies findByArtisanIdAndDeletedAtIsNull returns paginated active enrollments and excludes soft-deleted.
     */
    @Test
    @DisplayName("findByArtisanIdAndDeletedAtIsNull: returns paginated artisan enrollments and excludes soft-deleted")
    void findByArtisanIdAndDeletedAtIsNull_returnsPaginatedAndExcludesSoftDeleted() {
        User teacherUser = persistUser("teacher5@souklab.dz");
        Artisan teacher = persistArtisan(teacherUser, true);
        Formation f1 = persistFormation(teacher, "Course 1");
        Formation f2 = persistFormation(teacher, "Course 2");
        Formation f3 = persistFormation(teacher, "Course 3");

        User studentUser = persistUser("enrolled_student@souklab.dz");
        Artisan student = persistArtisan(studentUser, false);

        FormationEnrollment e1 = persistEnrollment(f1, student, EnrollmentStatus.CONFIRMED);
        FormationEnrollment e2 = persistEnrollment(f2, student, EnrollmentStatus.CANCELLED);
        FormationEnrollment e3 = persistEnrollment(f3, student, EnrollmentStatus.CONFIRMED);
        e3.setDeletedAt(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        Page<FormationEnrollment> page = enrollmentRepository.findByArtisanIdAndDeletedAtIsNull(
                student.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(FormationEnrollment::getId)
                .containsExactlyInAnyOrder(e1.getId(), e2.getId())
                .doesNotContain(e3.getId());
    }
}
