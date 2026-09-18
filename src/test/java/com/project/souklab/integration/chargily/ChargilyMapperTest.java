package com.project.souklab.integration.chargily;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChargilyMapperTest {
    @Test
    void mapsCheckoutRequestWithMetadataAndProviderResponse() throws Exception {
        ChargilyCheckoutRequest request = ChargilyCheckoutRequest.builder()
                .amount(1500).currency("DZD").successUrl("https://app/success")
                .failureUrl("https://app/failure").webhookUrl("https://app/webhook").locale("fr").feeAllocation("customer")
                .metadata(Map.of("payment_id", "payment-1")).build();

        String json = new ObjectMapper().writeValueAsString(ChargilyRequestMapper.from(request));
        assertThat(json).contains("\"amount\":1500", "\"currency\":\"dzd\"", "webhook_endpoint", "payment_id");

        ChargilyCheckoutResponse response = new ChargilyResponseMapper()
                .toCheckoutResponse(new ObjectMapper().readTree("{\"id\":\"co-1\",\"checkout_url\":\"https://pay/co-1\"}"));
        assertThat(response.getId()).isEqualTo("co-1");
        assertThat(response.getCheckoutUrl()).isEqualTo("https://pay/co-1");
    }

    @Test
    void rejectsProviderResponseWithoutCheckoutIdentifiers() throws Exception {
        assertThatThrownBy(() -> new ChargilyResponseMapper()
                .toCheckoutResponse(new ObjectMapper().readTree("{\"id\":\"co-1\"}")))
                .isInstanceOf(ChargilyProviderException.class);
    }
}
