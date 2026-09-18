package com.project.souklab.integration.chargily;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ChargilyCheckoutRequest {
    long amount;
    String currency;
    String successUrl;
    String failureUrl;
    String webhookUrl;
    String locale;
    String feeAllocation;
    Map<String, String> metadata;
}
