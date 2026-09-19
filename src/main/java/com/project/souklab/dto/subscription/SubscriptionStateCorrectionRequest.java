package com.project.souklab.dto.subscription;

import com.project.souklab.model.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionStateCorrectionRequest {
    @NotNull
    private SubscriptionStatus status;

    @NotBlank
    private String reason;
}
