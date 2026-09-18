package com.project.souklab.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FinancialReasonRequest {
    @NotBlank
    private String reason;
}
