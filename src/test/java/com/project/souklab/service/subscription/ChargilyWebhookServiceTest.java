package com.project.souklab.service.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanSubscriptionRepository;
import com.project.souklab.dao.ClientSubscriptionRepository;
import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dao.PaymentWebhookLogRepository;
import com.project.souklab.model.PaymentWebhookLog;
import com.project.souklab.model.Payment;
import com.project.souklab.model.PaymentStatus;
import com.project.souklab.model.SubscriptionStatus;
import com.project.souklab.model.ClientSubscription;
import com.project.souklab.model.WebhookProcessingStatus;
import com.project.souklab.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChargilyWebhookServiceTest {
    private ChargilyWebhookService service;
    private WebhookEventClaimService claimService;
    private PaymentWebhookLogRepository logRepository;
    private PaymentRepository paymentRepository;
    private ClientSubscriptionRepository clientSubscriptionRepository;
    private byte[] secret;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        secret = "webhook-secret".getBytes(StandardCharsets.UTF_8);
        properties.getChargily().setSecretKey(new String(secret, StandardCharsets.UTF_8));
        properties.getChargily().setWebhookEncryptionKey(Base64.getEncoder().encodeToString(new byte[32]));
        properties.getSubscription().setWebhookRetention(Duration.ofDays(90));
        properties.getSubscription().setLifecycleInterval(Duration.ofHours(1));
        Clock clock = Clock.fixed(Instant.parse("2026-09-18T00:00:00Z"), ZoneOffset.UTC);
        logRepository = mock(PaymentWebhookLogRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        clientSubscriptionRepository = mock(ClientSubscriptionRepository.class);
        claimService = mock(WebhookEventClaimService.class);
        service = new ChargilyWebhookService(
                new ObjectMapper(), new WebhookSecurityService(properties), logRepository,
                paymentRepository, mock(ArtisanSubscriptionRepository.class),
                clientSubscriptionRepository, mock(SubscriptionPlanRules.class),
                mock(NotificationService.class), claimService, properties, clock);
    }

    @Test
    void rejectsIncompleteAndStaleEventsBeforeClaiming() throws Exception {
        byte[] incomplete = "{\"id\":\"evt-1\",\"type\":\"checkout.paid\",\"data\":{}}".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> service.process(incomplete, signature(incomplete)))
                .isInstanceOf(MalformedWebhookException.class);

        byte[] stale = ("{\"id\":\"evt-2\",\"type\":\"checkout.paid\",\"created_at\":"
                + Instant.parse("2020-01-01T00:00:00Z").getEpochSecond()
                + ",\"data\":{\"id\":\"checkout-2\"}}").getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> service.process(stale, signature(stale)))
                .isInstanceOf(MalformedWebhookException.class);
        verify(claimService, never()).claim(any(), any(), any(), any());
    }

    @Test
    void validUnknownCheckoutIsClaimedAndIgnoredWithoutActivation() throws Exception {
        byte[] body = ("{\"id\":\"evt-3\",\"type\":\"checkout.paid\",\"created_at\":"
                + Instant.parse("2026-09-17T23:00:00Z").getEpochSecond()
                + ",\"data\":{\"id\":\"unknown-checkout\"}}").getBytes(StandardCharsets.UTF_8);
        PaymentWebhookLog log = new PaymentWebhookLog();
        log.setStatus(WebhookProcessingStatus.RECEIVED);
        when(claimService.claim("evt-3", ChargilyWebhookEvent.Checkout.PAID, "unknown-checkout", body)).thenReturn(true);
        when(claimService.acquireProcessing(any(), any())).thenReturn(true);
        when(logRepository.findByProviderEventId("evt-3")).thenReturn(Optional.of(log));

        service.process(body, signature(body));

        verify(claimService).claim("evt-3", ChargilyWebhookEvent.Checkout.PAID, "unknown-checkout", body);
        assertThat(log.getStatus()).isEqualTo(WebhookProcessingStatus.IGNORED);
    }

    @Test
    void failedCheckoutCancelsPendingSubscription() throws Exception {
        byte[] body = ("{\"id\":\"evt-4\",\"type\":\"checkout.failed\",\"created_at\":"
                + Instant.parse("2026-09-17T23:00:00Z").getEpochSecond()
                + ",\"data\":{\"id\":\"checkout-4\"}}").getBytes(StandardCharsets.UTF_8);
        PaymentWebhookLog log = new PaymentWebhookLog();
        log.setStatus(WebhookProcessingStatus.RECEIVED);
        Payment payment = new Payment();
        payment.setSubscriptionId("subscription-4");
        ClientSubscription subscription = new ClientSubscription();
        subscription.setStatus(SubscriptionStatus.PENDING);
        when(claimService.claim("evt-4", ChargilyWebhookEvent.Checkout.FAILED, "checkout-4", body)).thenReturn(true);
        when(claimService.acquireProcessing(any(), any())).thenReturn(true);
        when(logRepository.findByProviderEventId("evt-4")).thenReturn(Optional.of(log));
        when(paymentRepository.findByProviderCheckoutId("checkout-4")).thenReturn(Optional.of(payment));
        when(clientSubscriptionRepository.findWithLockById("subscription-4")).thenReturn(Optional.of(subscription));

        service.process(body, signature(body));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(log.getStatus()).isEqualTo(WebhookProcessingStatus.PROCESSED);
    }

    @Test
    void duplicateProcessedDeliveryDoesNotAcquireOrProcessAgain() throws Exception {
        byte[] body = ("{\"id\":\"evt-5\",\"type\":\"checkout.paid\",\"created_at\":"
                + Instant.parse("2026-09-17T23:00:00Z").getEpochSecond()
                + ",\"data\":{\"id\":\"checkout-5\"}}").getBytes(StandardCharsets.UTF_8);
        PaymentWebhookLog log = new PaymentWebhookLog();
        log.setStatus(WebhookProcessingStatus.PROCESSED);
        when(claimService.claim("evt-5", ChargilyWebhookEvent.Checkout.PAID, "checkout-5", body)).thenReturn(false);
        when(logRepository.findByProviderEventId("evt-5")).thenReturn(Optional.of(log));

        service.process(body, signature(body));

        verify(claimService, never()).acquireProcessing(any(), any());
        verifyNoInteractions(paymentRepository);
    }

    private String signature(byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        StringBuilder result = new StringBuilder();
        for (byte value : mac.doFinal(body)) result.append(String.format("%02x", value));
        return result.toString();
    }
}
