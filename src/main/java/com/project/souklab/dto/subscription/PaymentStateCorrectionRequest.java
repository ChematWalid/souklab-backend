package com.project.souklab.dto.subscription;

import com.project.souklab.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentStateCorrectionRequest {
    @NotNull
    private PaymentStatus status;

    @NotBlank
    private String reason;
}
