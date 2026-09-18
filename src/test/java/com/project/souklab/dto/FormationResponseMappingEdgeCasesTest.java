package com.project.souklab.dto;

import com.project.souklab.dto.formation.FormationEnrollmentResponseDTO;
import com.project.souklab.dto.formation.FormationReviewResponseDTO;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.FormationEnrollment;
import com.project.souklab.model.FormationReview;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormationResponseMappingEdgeCasesTest {

    @Test
    void mapsEnrollmentRelationshipsAndDisplayNameFallbacks() {
        assertThat(FormationEnrollmentResponseDTO.from(null)).isNull();
        assertThat(FormationEnrollmentResponseDTO.from(FormationEnrollment.builder().build()).getFormationId()).isNull();

        User user = User.builder().email("artisan@example.test").firstName("First").lastName("Last").build();
        Artisan artisan = Artisan.builder().id("artisan").user(user).build();
        FormationEnrollment withName = FormationEnrollment.builder().artisan(artisan).build();
        assertThat(FormationEnrollmentResponseDTO.from(withName).getArtisanName()).isEqualTo("First Last");

        user.setFirstName(null);
        user.setLastName(null);
        assertThat(FormationEnrollmentResponseDTO.from(withName).getArtisanName()).isEqualTo("artisan@example.test");
        FormationEnrollment withoutUser = FormationEnrollment.builder().artisan(Artisan.builder().id("artisan").build()).build();
        assertThat(FormationEnrollmentResponseDTO.from(withoutUser).getArtisanName()).isNull();
    }

    @Test
    void mapsReviewAdministratorNameAndNullAdministrator() {
        assertThat(FormationReviewResponseDTO.from(null)).isNull();
        FormationReview noAdmin = FormationReview.builder().build();
        assertThat(FormationReviewResponseDTO.from(noAdmin).getAdminName()).isNull();

        User admin = User.builder().email("admin@example.test").firstName("Admin").build();
        FormationReview firstOnly = FormationReview.builder().admin(admin).build();
        assertThat(FormationReviewResponseDTO.from(firstOnly).getAdminName()).isEqualTo("Admin");
        admin.setFirstName(null);
        admin.setLastName("Last");
        assertThat(FormationReviewResponseDTO.from(firstOnly).getAdminName()).isEqualTo("Last");
        admin.setLastName(null);
        assertThat(FormationReviewResponseDTO.from(firstOnly).getAdminName()).isEqualTo("admin@example.test");
    }
}
