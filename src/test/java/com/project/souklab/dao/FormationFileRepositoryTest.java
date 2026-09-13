package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationFile;
import com.project.souklab.model.FormationStatus;
import com.project.souklab.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test verifying {@link FormationFileRepository} course attachment retrieval,
 * formation ownership scoping, and soft-delete exclusions.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FormationFileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FormationFileRepository fileRepository;

    /**
     * Persists an active test user.
     */
    private User persistUser(String email) {
        User user = User.builder()
                .email(email)
                .firstName("Teacher")
                .lastName("Artisan")
                .status(AccountStatus.ACTIVE)
                .build();
        return entityManager.persist(user);
    }

    /**
     * Persists an artisan.
     */
    private Artisan persistArtisan(User user) {
        Artisan artisan = Artisan.builder()
                .user(user)
                .city("Constantine")
                .isTeacher(true)
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
                .description("Course syllabus and attachments")
                .status(FormationStatus.PUBLISHED)
                .price(8000)
                .currency("DZD")
                .maxParticipants(8)
                .durationHours(3)
                .isOnline(true)
                .scheduledAt(LocalDateTime.now().plusDays(3))
                .build();
        return entityManager.persist(formation);
    }

    /**
     * Persists a course file attachment.
     */
    private FormationFile persistFile(Formation formation, String filename, String storageKey) {
        FormationFile file = FormationFile.builder()
                .formation(formation)
                .originalFilename(filename)
                .storageKey(storageKey)
                .contentType("application/pdf")
                .fileSize(1024L * 1024L)
                .build();
        return entityManager.persist(file);
    }

    /**
     * Verifies findByFormationIdAndDeletedAtIsNull returns all non-deleted files for a formation.
     */
    @Test
    @DisplayName("findByFormationIdAndDeletedAtIsNull: returns active files and excludes soft-deleted")
    void findByFormationIdAndDeletedAtIsNull_returnsActiveFiles() {
        User user = persistUser("files_teacher@souklab.dz");
        Artisan author = persistArtisan(user);
        Formation formation1 = persistFormation(author, "Wood Inlay Art");
        Formation formation2 = persistFormation(author, "Loom Weaving");

        FormationFile f1 = persistFile(formation1, "syllabus.pdf", "keys/f1_syllabus.pdf");
        FormationFile f2 = persistFile(formation1, "patterns.pdf", "keys/f1_patterns.pdf");
        FormationFile f3 = persistFile(formation1, "deleted_guide.pdf", "keys/f1_deleted.pdf");
        f3.setDeletedAt(LocalDateTime.now());

        FormationFile fOther = persistFile(formation2, "other.pdf", "keys/f2_other.pdf");

        entityManager.flush();
        entityManager.clear();

        List<FormationFile> files = fileRepository.findByFormationIdAndDeletedAtIsNull(formation1.getId());

        assertThat(files).hasSize(2)
                .extracting(FormationFile::getId)
                .containsExactlyInAnyOrder(f1.getId(), f2.getId())
                .doesNotContain(f3.getId(), fOther.getId());
    }

    /**
     * Verifies findByIdAndFormationIdAndDeletedAtIsNull retrieves file by ID and formation ID and excludes soft-deleted.
     */
    @Test
    @DisplayName("findByIdAndFormationIdAndDeletedAtIsNull: retrieves active file matching parent formation")
    void findByIdAndFormationIdAndDeletedAtIsNull_verifiesMatchingParentAndActive() {
        User user = persistUser("files_teacher2@souklab.dz");
        Artisan author = persistArtisan(user);
        Formation formation1 = persistFormation(author, "Calligraphy Basics");
        Formation formation2 = persistFormation(author, "Advanced Calligraphy");

        FormationFile activeFile = persistFile(formation1, "intro.pdf", "keys/intro.pdf");
        FormationFile deletedFile = persistFile(formation1, "old.pdf", "keys/old.pdf");
        deletedFile.setDeletedAt(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();

        Optional<FormationFile> found = fileRepository.findByIdAndFormationIdAndDeletedAtIsNull(
                activeFile.getId(), formation1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalFilename()).isEqualTo("intro.pdf");

        Optional<FormationFile> wrongParent = fileRepository.findByIdAndFormationIdAndDeletedAtIsNull(
                activeFile.getId(), formation2.getId());
        assertThat(wrongParent).isEmpty();

        Optional<FormationFile> deleted = fileRepository.findByIdAndFormationIdAndDeletedAtIsNull(
                deletedFile.getId(), formation1.getId());
        assertThat(deleted).isEmpty();
    }
}
