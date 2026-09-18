package com.project.souklab.integration.chargily;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Locale;

@Value
@Builder
public class ChargilyRequestMapper {
    @JsonProperty("amount") long amount;
    @JsonProperty("currency") String currency;
    @JsonProperty("success_url") String successUrl;
    @JsonProperty("failure_url") String failureUrl;
    @JsonProperty("webhook_endpoint") String webhookUrl;
    @JsonProperty("locale") String locale;
    @JsonProperty("chargily_pay_fees_allocation") String feeAllocation;
    @JsonProperty("metadata") Map<String, String> metadata;

    public static ChargilyRequestMapper from(ChargilyCheckoutRequest request) {
        return ChargilyRequestMapper.builder().amount(request.getAmount()).currency(request.getCurrency().toLowerCase(Locale.ROOT))
                .successUrl(request.getSuccessUrl()).failureUrl(request.getFailureUrl()).locale(request.getLocale())
                .webhookUrl(request.getWebhookUrl())
                .feeAllocation(request.getFeeAllocation()).metadata(request.getMetadata()).build();
    }
}
