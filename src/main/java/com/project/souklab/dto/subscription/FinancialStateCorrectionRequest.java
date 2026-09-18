package com.project.souklab.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FinancialStateCorrectionRequest {
    @NotBlank
    private String status;

    @NotBlank
    private String reason;
}
