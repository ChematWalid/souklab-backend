package com.project.souklab.dao;


import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanProfileView;
import com.project.souklab.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest slice verifying ArtisanProfileViewRepository query derivation and composite unique
 * constraint enforcement on (viewer_id, artisan_id) against embedded H2.
 */
@DataJpaTest
@TestPropertySource(locations = TestJpaProperties.H2_PROPERTIES, properties = {
        TestJpaProperties.DISABLE_HIBERNATE_SEARCH,
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ArtisanProfileViewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArtisanProfileViewRepository viewRepository;

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
                .city("Oran")
                .build();
        return entityManager.persist(artisan);
    }

    /**
     * Persists an ArtisanProfileView representing a visit from viewer to artisan.
     */
    private ArtisanProfileView persistView(User viewer, Artisan artisan) {
        ArtisanProfileView view = ArtisanProfileView.builder()
                .viewer(viewer)
                .artisan(artisan)
                .build();
        return entityManager.persist(view);
    }

    /**
     * Verifies that the composite unique constraint on (viewer_id, artisan_id) is enforced
     * by the database, throwing ConstraintViolationException on duplicate profile views.
     */
    @Test
    @DisplayName("composite unique constraint (viewer_id, artisan_id): throws ConstraintViolationException on duplicate view")
    void persistArtisanProfileView_whenDuplicateViewerAndArtisan_throwsConstraintViolationException() {
        User viewer = persistUser("viewer1@souklab.com");
        User artisanUser = persistUser("artisan_view1@souklab.com");
        Artisan artisan = persistArtisan(artisanUser);

        persistView(viewer, artisan);
        entityManager.flush();

        persistView(viewer, artisan);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * Verifies existsByViewerIdAndArtisanId returns true when a profile view record exists
     * for the given viewer and artisan IDs, and false otherwise.
     */
    @Test
    @DisplayName("existsByViewerIdAndArtisanId: returns true when view exists, false when absent")
    void existsByViewerIdAndArtisanId_verifiesPresenceAndAbsence() {
        User viewer = persistUser("viewer2@souklab.com");
        User otherViewer = persistUser("viewer3@souklab.com");
        User artisanUser = persistUser("artisan_view2@souklab.com");
        Artisan artisan = persistArtisan(artisanUser);

        persistView(viewer, artisan);
        entityManager.flush();
        entityManager.clear();

        boolean exists = viewRepository.existsByViewerIdAndArtisanId(viewer.getId(), artisan.getId());
        assertThat(exists).isTrue();

        boolean nonExistentViewer = viewRepository.existsByViewerIdAndArtisanId(otherViewer.getId(), artisan.getId());
        assertThat(nonExistentViewer).isFalse();

        boolean nonExistentArtisan = viewRepository.existsByViewerIdAndArtisanId(viewer.getId(), "non-existent-artisan-id");
        assertThat(nonExistentArtisan).isFalse();
    }
}
