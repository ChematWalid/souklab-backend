package com.project.souklab.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ManualSubscriptionGrantRequest {
    @NotBlank
    private String accountId;
    @NotBlank
    private String planId;
    @NotBlank
    private String reason;
}
