package com.project.souklab.dto;

import com.project.souklab.dto.formation.FormationEnrollmentDetailDTO;
import com.project.souklab.dto.formation.FormationEnrollmentResponseDTO;
import com.project.souklab.dto.formation.FormationFileDescriptorDTO;
import com.project.souklab.dto.formation.FormationFileResponseDTO;
import com.project.souklab.dto.formation.FormationPublicViewDTO;
import com.project.souklab.dto.formation.FormationResponseDTO;
import com.project.souklab.dto.formation.FormationReviewResponseDTO;
import com.project.souklab.dto.formation.FormationSummaryDTO;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationEnrollment;
import com.project.souklab.model.FormationFile;
import com.project.souklab.model.FormationReview;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FormationDtoEdgeCasesTest {

    @Test
    void factoriesReturnNullForNullEntities() {
        assertThat(FormationSummaryDTO.from(null, 0)).isNull();
        assertThat(FormationFileDescriptorDTO.from(null, null)).isNull();
        assertThat(FormationFileResponseDTO.from(null, null)).isNull();
        assertThat(FormationEnrollmentResponseDTO.from(null)).isNull();
        assertThat(FormationEnrollmentDetailDTO.from(null, 0)).isNull();
        assertThat(FormationReviewResponseDTO.from(null)).isNull();
        assertThat(FormationResponseDTO.from(null, null, null, 0, null)).isNull();
        assertThat(FormationPublicViewDTO.from(null, null, 0, false, false)).isNull();
    }

    @Test
    void enrollmentAndReviewFactoriesHandleMissingRelationships() {
        FormationEnrollment enrollment = new FormationEnrollment();
        enrollment.setId("enrollment");
        FormationEnrollmentResponseDTO response = FormationEnrollmentResponseDTO.from(enrollment);
        assertThat(response.getFormationId()).isNull();
        assertThat(response.getArtisanId()).isNull();
        assertThat(response.getArtisanName()).isNull();

        FormationReview review = new FormationReview();
        review.setId("review");
        FormationReviewResponseDTO reviewDto = FormationReviewResponseDTO.from(review);
        assertThat(reviewDto.getAdminId()).isNull();
        assertThat(reviewDto.getAdminName()).isNull();
    }

    @Test
    void formationFactoriesUseEmptyOptionalCollectionsAndFallbackPrefixes() {
        Formation formation = new Formation();
        formation.setId("formation");
        formation.setMaxParticipants(2);
        FormationResponseDTO response = FormationResponseDTO.from(formation, null, null, 0, " ");
        assertThat(response.getFiles()).isEmpty();
        assertThat(response.getReviews()).isEmpty();

        FormationPublicViewDTO publicView = FormationPublicViewDTO.from(formation, null, 4, false, false);
        assertThat(publicView.getFiles()).isEmpty();
        assertThat(publicView.getAvailableSeats()).isZero();

        FormationFile file = new FormationFile();
        file.setId("file");
        file.setStorageKey("key.pdf");
        file.setOriginalFilename("name.pdf");
        assertThat(FormationFileResponseDTO.from(file, "/files").getDownloadUrl()).isEqualTo("/files/key.pdf");
        assertThat(FormationFileResponseDTO.from(file, "/files/").getDownloadUrl()).isEqualTo("/files/key.pdf");
        assertThat(FormationFileResponseDTO.from(file, " ").getDownloadUrl()).contains("key.pdf");
        assertThat(FormationFileDescriptorDTO.from(file, null).getDownloadUrl()).isNull();
        assertThat(FormationResponseDTO.from(formation, List.of(file), List.of(), 0).getFiles()).hasSize(1);
    }
}
