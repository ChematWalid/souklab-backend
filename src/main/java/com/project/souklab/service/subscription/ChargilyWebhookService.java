package com.project.souklab.service.subscription;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.souklab.model.User;

import com.project.souklab.analytics.AnalyticsEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.dao.ArtisanSubscriptionRepository;
import com.project.souklab.analytics.ActivityEventService;
import com.project.souklab.dao.ClientSubscriptionRepository;
import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dao.PaymentWebhookLogRepository;
import com.project.souklab.config.AppProperties;
import com.project.souklab.integration.chargily.ChargilyWebhookPayload;
import com.project.souklab.model.ArtisanSubscription;
import com.project.souklab.model.ClientSubscription;
import com.project.souklab.model.Payment;
import com.project.souklab.model.PaymentStatus;
import com.project.souklab.model.PaymentWebhookLog;
import com.project.souklab.model.SubscriptionStatus;
import com.project.souklab.model.WebhookProcessingStatus;
import com.project.souklab.model.NotificationType;
import com.project.souklab.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChargilyWebhookService {
    private final ObjectMapper objectMapper;
    private final WebhookSecurityService webhookSecurityService;
    private final PaymentWebhookLogRepository webhookLogRepository;
    private final PaymentRepository paymentRepository;
    private final ArtisanSubscriptionRepository artisanSubscriptionRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final SubscriptionPlanRules rules;
    private final NotificationService notificationService;
    private final WebhookEventClaimService webhookEventClaimService;
    private final AppProperties appProperties;
    private final Clock clock;
    private ActivityEventService activityEventService;

    @Autowired(required = false)
    void setActivityEventService(ActivityEventService value) { this.activityEventService = value; }

    @Transactional
    public void process(byte[] rawBody, String signature) {
        if (!webhookSecurityService.isValidSignature(rawBody, signature)) {
            throw new InvalidWebhookSignatureException();
        }
        ChargilyWebhookPayload payload = parse(rawBody);
        ChargilyWebhookEvent.Checkout eventType = ChargilyWebhookEvent.Checkout.fromValue(payload.getType()).orElse(null);
        if (payload.getId() == null || payload.getId().isBlank() || eventType == null
                || payload.getData() == null || !payload.getData().isObject() || payload.getCreatedAt() == null) {
            throw new MalformedWebhookException("Unsupported or incomplete webhook event");
        }
        Instant eventCreatedAt;
        try {
            eventCreatedAt = Instant.ofEpochSecond(payload.getCreatedAt());
        } catch (RuntimeException exception) {
            throw new MalformedWebhookException("Webhook event timestamp is invalid");
        }
        if (appProperties.getSubscription().getWebhookRetention() != null
                && eventCreatedAt.isBefore(Instant.now(clock).minus(appProperties.getSubscription().getWebhookRetention()))) {
            throw new MalformedWebhookException("Webhook event is stale");
        }
        String checkoutId = payload.getData() == null ? null : new WebhookJsonValue(payload.getData()).text("id", "checkout_id");
        if (checkoutId == null || checkoutId.isBlank()) {
            throw new MalformedWebhookException("Webhook checkout identifier is missing");
        }
        boolean claimed = webhookEventClaimService.claim(payload.getId(), eventType, checkoutId, rawBody);
        if (!claimed) {
            PaymentWebhookLog existing = webhookLogRepository.findByProviderEventId(payload.getId()).orElse(null);
            if (existing == null || existing.getStatus() != WebhookProcessingStatus.FAILED) {
                return;
            }
        }
        LocalDateTime staleBefore = LocalDateTime.now(clock).minus(appProperties.getSubscription().getLifecycleInterval());
        if (!webhookEventClaimService.acquireProcessing(payload.getId(), staleBefore)) {
            return;
        }
        PaymentWebhookLog log = webhookLogRepository.findByProviderEventId(payload.getId()).orElseThrow();
        log.setStatus(WebhookProcessingStatus.PROCESSING);
        try {
            paymentRepository.findByProviderCheckoutId(checkoutId).ifPresent(payment -> apply(payment, eventType, log));
            if (log.getStatus() == WebhookProcessingStatus.PROCESSING) {
                log.setStatus(WebhookProcessingStatus.IGNORED);
            }
        } catch (RuntimeException exception) {
            webhookEventClaimService.markFailed(payload.getId());
            throw exception;
        }
    }

    private void apply(Payment payment, ChargilyWebhookEvent.Checkout eventType, PaymentWebhookLog log) {
        if (payment.getStatus() == PaymentStatus.PAID
                || payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.CANCELED
                || payment.getStatus() == PaymentStatus.EXPIRED
                || payment.getStatus() == PaymentStatus.MANUALLY_GRANTED) {
            log.setStatus(WebhookProcessingStatus.PROCESSED);
            return;
        }
        if (eventType == ChargilyWebhookEvent.Checkout.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            activateSubscription(payment);
            notificationService.createForUser(payment.getAccount(), "Your subscription payment was successful.", NotificationType.PAYMENT_SUCCESS, payment.getId());
        } else if (eventType == ChargilyWebhookEvent.Checkout.FAILED) {
            payment.setStatus(PaymentStatus.FAILED);
            cancelPendingSubscription(payment);
            notificationService.createForUser(payment.getAccount(), "Your subscription payment failed.", NotificationType.PAYMENT_FAILED, payment.getId());
        } else {
            payment.setStatus(PaymentStatus.CANCELED);
            cancelPendingSubscription(payment);
            notificationService.createForUser(payment.getAccount(), "Your subscription checkout was canceled.", NotificationType.CHECKOUT_CANCELED, payment.getId());
        }
        if (activityEventService != null) {
            activityEventService.record(AnalyticsEvent.Payment.State.TRANSITION, payment.getAccount().getId(), payment.getId(),
                    Map.of("providerEvent", eventType.value(), "status", payment.getStatus().value()));
            if (payment.getStatus() == PaymentStatus.PAID) {
                activityEventService.record(AnalyticsEvent.Subscription.ACTIVATED, payment.getAccount().getId(),
                        payment.getSubscriptionId(), Map.of("paymentId", payment.getId()));
            }
        }
        log.setStatus(WebhookProcessingStatus.PROCESSED);
    }

    private void cancelPendingSubscription(Payment payment) {
        artisanSubscriptionRepository.findWithLockById(payment.getSubscriptionId()).ifPresent(subscription -> {
            if (subscription.getStatus() == SubscriptionStatus.PENDING) {
                rules.requireTransition(subscription.getStatus(), SubscriptionStatus.CANCELED);
                subscription.setStatus(SubscriptionStatus.CANCELED);
                recordSubscriptionCancellation(subscription.getAccount(), subscription.getId());
            }
        });
        clientSubscriptionRepository.findWithLockById(payment.getSubscriptionId()).ifPresent(subscription -> {
            if (subscription.getStatus() == SubscriptionStatus.PENDING) {
                rules.requireTransition(subscription.getStatus(), SubscriptionStatus.CANCELED);
                subscription.setStatus(SubscriptionStatus.CANCELED);
                recordSubscriptionCancellation(subscription.getAccount(), subscription.getId());
            }
        });
    }

    private void recordSubscriptionCancellation(User account, String subscriptionId) {
        if (activityEventService != null && account != null) {
            activityEventService.record(AnalyticsEvent.Subscription.CANCELED, account.getId(), subscriptionId,
                    Map.of("status", SubscriptionStatus.CANCELED.value(), "source", AnalyticsEvent.Source.PAYMENT_WEBHOOK.value()));
        }
    }

    private void activateSubscription(Payment payment) {
        LocalDateTime startsAt = LocalDateTime.now(clock);
        artisanSubscriptionRepository.findWithLockById(payment.getSubscriptionId()).ifPresent(subscription -> {
            activate(subscription, startsAt);
            if (subscription.getAccount().getArtisan() != null) {
                subscription.getAccount().getArtisan().setPremium(true);
            }
        });
        clientSubscriptionRepository.findWithLockById(payment.getSubscriptionId()).ifPresent(subscription -> {
            activate(subscription, startsAt);
            if (subscription.getAccount().getClient() != null) {
                subscription.getAccount().getClient().setPremium(true);
            }
        });
    }

    private void activate(ArtisanSubscription subscription, LocalDateTime startsAt) {
        rules.requireTransition(subscription.getStatus(), SubscriptionStatus.ACTIVE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartsAt(startsAt);
        subscription.setExpiresAt(rules.expiryFrom(startsAt, subscription.getBillingPeriod()));
    }

    private void activate(ClientSubscription subscription, LocalDateTime startsAt) {
        rules.requireTransition(subscription.getStatus(), SubscriptionStatus.ACTIVE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartsAt(startsAt);
        subscription.setExpiresAt(rules.expiryFrom(startsAt, subscription.getBillingPeriod()));
    }

    private ChargilyWebhookPayload parse(byte[] rawBody) {
        try {
            if (rawBody == null || rawBody.length == 0) {
                throw new MalformedWebhookException("Webhook body is empty");
            }
            return objectMapper.reader()
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue(rawBody, ChargilyWebhookPayload.class);
        } catch (JsonProcessingException exception) {
            throw new MalformedWebhookException("Webhook body is malformed");
        } catch (IOException exception) {
            throw new MalformedWebhookException("Webhook body could not be read");
        }
    }
}
