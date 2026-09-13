package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationReview;
import com.project.souklab.model.FormationReviewDecision;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test verifying {@link FormationReviewRepository} query execution and
 * chronological descending ordering of moderation reviews.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FormationReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FormationReviewRepository reviewRepository;

    /**
     * Persists an active test user.
     */
    private User persistUser(String email, String role) {
        User user = User.builder()
                .email(email)
                .firstName("Test")
                .lastName(role)
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
                .city("Oran")
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
                .description("Formation under moderation review")
                .status(FormationStatus.PENDING_REVIEW)
                .price(12000)
                .currency("DZD")
                .maxParticipants(15)
                .durationHours(5)
                .isOnline(false)
                .scheduledAt(LocalDateTime.now().plusDays(10))
                .build();
        return entityManager.persist(formation);
    }

    /**
     * Persists a formation review record.
     */
    private FormationReview persistReview(Formation formation, User admin, FormationReviewDecision decision,
                                          String comment, LocalDateTime reviewedAt) {
        FormationReview review = FormationReview.builder()
                .formation(formation)
                .admin(admin)
                .decision(decision)
                .comment(comment)
                .reviewedAt(reviewedAt)
                .build();
        return entityManager.persist(review);
    }

    /**
     * Verifies findByFormationIdOrderByReviewedAtDesc returns reviews ordered descending by review timestamp.
     */
    @Test
    @DisplayName("findByFormationIdOrderByReviewedAtDesc: returns reviews in descending chronological order")
    void findByFormationIdOrderByReviewedAtDesc_returnsDescendingOrder() {
        User authorUser = persistUser("author_rev@souklab.dz", "Author");
        User adminUser1 = persistUser("admin1@souklab.dz", "Admin");
        User adminUser2 = persistUser("admin2@souklab.dz", "Admin");

        Artisan author = persistArtisan(authorUser);
        Formation formation1 = persistFormation(author, "Traditional Basketry");
        Formation formation2 = persistFormation(author, "Straw Hats");

        LocalDateTime time1 = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2026, 9, 5, 14, 30);
        LocalDateTime time3 = LocalDateTime.of(2026, 9, 10, 9, 15);

        FormationReview r1 = persistReview(formation1, adminUser1, FormationReviewDecision.REJECTED, "Needs clearer syllabus", time1);
        FormationReview r2 = persistReview(formation1, adminUser2, FormationReviewDecision.APPROVED, "Updated syllabus looks complete", time3);
        FormationReview rOther = persistReview(formation2, adminUser1, FormationReviewDecision.APPROVED, "Looks good", time2);

        entityManager.flush();
        entityManager.clear();

        List<FormationReview> reviews = reviewRepository.findByFormationIdOrderByReviewedAtDesc(formation1.getId());

        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getId()).isEqualTo(r2.getId());
        assertThat(reviews.get(0).getDecision()).isEqualTo(FormationReviewDecision.APPROVED);
        assertThat(reviews.get(1).getId()).isEqualTo(r1.getId());
        assertThat(reviews.get(1).getDecision()).isEqualTo(FormationReviewDecision.REJECTED);
        assertThat(reviews).extracting(FormationReview::getId).doesNotContain(rOther.getId());
    }
}
