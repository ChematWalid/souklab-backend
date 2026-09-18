package com.project.souklab.dto;

import com.project.souklab.dto.auth.CompleteProfileRequestDTO;
import com.project.souklab.dto.auth.LoginDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDtoEdgeCasesTest {

    @Test
    void loginIdentifierPrefersTrimmedEmailThenUsername() {
        LoginDTO dto = new LoginDTO();
        assertThat(dto.getLoginIdentifier()).isNull();

        dto.setUsername("  artisan  ");
        assertThat(dto.getLoginIdentifier()).isEqualTo("artisan");

        dto.setEmail("  artisan@example.test  ");
        assertThat(dto.getLoginIdentifier()).isEqualTo("artisan@example.test");

        dto.setEmail(" ");
        dto.setUsername(" ");
        assertThat(dto.getLoginIdentifier()).isNull();
    }

    @Test
    void completeProfileResolvesRegionIdThenLegacyRegion() {
        CompleteProfileRequestDTO dto = new CompleteProfileRequestDTO();
        dto.setRegion("legacy-region");
        assertThat(dto.resolveRegionId()).isEqualTo("legacy-region");

        dto.setRegionId("region-id");
        assertThat(dto.resolveRegionId()).isEqualTo("region-id");

        dto.setRegionId(" ");
        assertThat(dto.resolveRegionId()).isEqualTo("legacy-region");
    }
}
