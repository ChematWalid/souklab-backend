package com.project.souklab.integration.payment;

import com.project.souklab.integration.chargily.ChargilyCheckoutClient;
import com.project.souklab.integration.chargily.ChargilyCheckoutRequest;
import com.project.souklab.integration.chargily.ChargilyCheckoutResponse;
import com.project.souklab.model.PaymentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargilyPaymentGatewayProviderTest {

    @Mock
    private ChargilyCheckoutClient checkoutClient;

    @InjectMocks
    private ChargilyPaymentGatewayProvider provider;

    @Test
    @DisplayName("getProvider: returns CHARGILY")
    void getProvider_returnsChargily() {
        assertThat(provider.getProvider()).isEqualTo(PaymentProvider.CHARGILY);
    }

    @Test
    @DisplayName("createCheckout: maps command correctly and calls Chargily client")
    void createCheckout_mapsCommandCorrectly() {
        ChargilyCheckoutResponse fakeResponse = new ChargilyCheckoutResponse();
        fakeResponse.setId("chk_123");
        fakeResponse.setCheckoutUrl("https://pay.chargily.net/checkout/chk_123");

        when(checkoutClient.createCheckout(any(ChargilyCheckoutRequest.class))).thenReturn(fakeResponse);

        PaymentCheckoutCommand command = new PaymentCheckoutCommand(
                "plan_pro",
                "Pro Plan",
                BigDecimal.valueOf(2500),
                "artisan@souklab.dz",
                "Artisan Name",
                "https://souklab.dz/success",
                "https://souklab.dz/failure",
                "https://souklab.dz/webhook",
                Map.of("accountId", "acc_123")
        );

        PaymentCheckoutResult result = provider.createCheckout(command);

        assertThat(result.checkoutId()).isEqualTo("chk_123");
        assertThat(result.checkoutUrl()).isEqualTo("https://pay.chargily.net/checkout/chk_123");

        ArgumentCaptor<ChargilyCheckoutRequest> captor = ArgumentCaptor.forClass(ChargilyCheckoutRequest.class);
        verify(checkoutClient).createCheckout(captor.capture());
        ChargilyCheckoutRequest captured = captor.getValue();
        assertThat(captured.getAmount()).isEqualTo(2500L);
        assertThat(captured.getCurrency()).isEqualTo("dzd");
        assertThat(captured.getSuccessUrl()).isEqualTo("https://souklab.dz/success");
        assertThat(captured.getMetadata()).containsEntry("accountId", "acc_123");
    }
}
