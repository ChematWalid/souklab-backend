package com.project.souklab.dto.formation;

import com.project.souklab.model.FormationFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormationFileResponseDTOTest {
    @Test
    void mapsConfiguredPrefixesAndNullInput() {
        FormationFile file = FormationFile.builder()
                .storageKey("course/lesson.pdf")
                .originalFilename("lesson.pdf")
                .contentType("application/pdf")
                .fileSize(12)
                .build();

        assertThat(FormationFileResponseDTO.from(file, "/download").getDownloadUrl())
                .isEqualTo("/download/course/lesson.pdf");
        assertThat(FormationFileResponseDTO.from(file, "/download/").getDownloadUrl())
                .isEqualTo("/download/course/lesson.pdf");
        assertThat(FormationFileResponseDTO.from(file, null).getDownloadUrl())
                .isEqualTo("/api/v1/files/course/lesson.pdf");
        assertThat(FormationFileResponseDTO.from(null)).isNull();
    }
}
