package com.project.souklab.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionCheckoutRequest {
    @NotBlank
    private String planId;
    private String successUrl;
    private String failureUrl;
}
