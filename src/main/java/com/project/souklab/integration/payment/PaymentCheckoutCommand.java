package com.project.souklab.integration.payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Command carrying parameters needed by an external payment gateway provider
 * to initiate a hosted or API checkout session.
 */
public record PaymentCheckoutCommand(
        String planId,
        String planName,
        BigDecimal amountDzd,
        String customerEmail,
        String customerName,
        String successUrl,
        String failureUrl,
        String webhookUrl,
        Map<String, Object> metadata
) {
}
