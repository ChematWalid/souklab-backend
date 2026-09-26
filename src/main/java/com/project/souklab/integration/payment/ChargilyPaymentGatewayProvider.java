package com.project.souklab.integration.payment;

import com.project.souklab.integration.chargily.ChargilyCheckoutClient;
import com.project.souklab.integration.chargily.ChargilyCheckoutRequest;
import com.project.souklab.integration.chargily.ChargilyCheckoutResponse;
import com.project.souklab.model.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Chargily Pay V2 implementation of {@link PaymentGatewayProvider}.
 */
@Component
@RequiredArgsConstructor
public class ChargilyPaymentGatewayProvider implements PaymentGatewayProvider {

    private final ChargilyCheckoutClient checkoutClient;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.CHARGILY;
    }

    @Override
    public PaymentCheckoutResult createCheckout(PaymentCheckoutCommand command) {
        Map<String, String> stringMeta = new HashMap<>();
        if (command.metadata() != null) {
            command.metadata().forEach((k, v) -> {
                if (v != null) {
                    stringMeta.put(k, String.valueOf(v));
                }
            });
        }

        ChargilyCheckoutRequest request = ChargilyCheckoutRequest.builder()
                .amount(command.amountDzd().longValue())
                .currency("dzd")
                .successUrl(command.successUrl())
                .failureUrl(command.failureUrl())
                .webhookUrl(command.webhookUrl())
                .locale("fr")
                .feeAllocation("customer")
                .metadata(stringMeta)
                .build();

        ChargilyCheckoutResponse response = checkoutClient.createCheckout(request);
        return new PaymentCheckoutResult(response.getId(), response.getCheckoutUrl());
    }
}
