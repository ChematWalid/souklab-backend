package com.project.souklab.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;

import com.project.souklab.analytics.AnalyticsEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.dao.ArtisanSubscriptionRepository;
import com.project.souklab.dao.ClientSubscriptionRepository;
import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dao.SubscriptionPlanRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.analytics.ActivityEventService;
import com.project.souklab.dto.subscription.FinancialReasonRequest;
import com.project.souklab.dto.subscription.FinancialStateCorrectionRequest;
import com.project.souklab.dto.subscription.ManualSubscriptionGrantRequest;
import com.project.souklab.dto.subscription.SubscriptionPlanSnapshot;
import com.project.souklab.dto.subscription.SubscriptionResponse;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.ArtisanSubscription;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.ClientSubscription;
import com.project.souklab.model.Payment;
import com.project.souklab.model.PaymentProvider;
import com.project.souklab.model.PaymentStatus;
import com.project.souklab.model.SubscriberType;
import com.project.souklab.model.SubscriptionPlan;
import com.project.souklab.model.SubscriptionStatus;
import com.project.souklab.model.User;
import com.project.souklab.model.NotificationType;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final ArtisanSubscriptionRepository artisanSubscriptions;
    private final ClientSubscriptionRepository clientSubscriptions;
    private final PaymentRepository payments;
    private final SubscriptionPlanRules rules;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private ActivityEventService activityEventService;

    @Autowired(required = false)
    void setActivityEventService(ActivityEventService value) { this.activityEventService = value; }

    @Transactional
    public SubscriptionResponse grant(ManualSubscriptionGrantRequest request) {
        User actor = currentUserProvider.requireCurrentUser();
        User target = userRepository.findById(request.getAccountId()).orElseThrow(() -> new ResourceNotFoundException("Target account not found"));
        SubscriptionPlan plan = planRepository.findById(request.getPlanId()).filter(SubscriptionPlan::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));
        if (plan.getSubscriberType() == SubscriberType.ARTISAN && userRepository.findById(target.getId()).map(User::getArtisan).orElse(null) == null) {
            throw new BadRequestException("Target account does not have an artisan profile");
        }
        if (plan.getSubscriberType() == SubscriberType.CLIENT && userRepository.findById(target.getId()).map(User::getClient).orElse(null) == null) {
            throw new BadRequestException("Target account does not have a client profile");
        }
        LocalDateTime starts = LocalDateTime.now(clock);
        String snapshot = snapshot(plan);
        SubscriptionResponse response;
        String subscriptionId;
        if (plan.getSubscriberType() == SubscriberType.ARTISAN) {
            if (artisanSubscriptions.countByAccountIdAndStatus(target.getId(), SubscriptionStatus.ACTIVE) > 0) throw new BadRequestException("Target already has an active subscription");
            ArtisanSubscription subscription = new ArtisanSubscription();
            populate(subscription, target, plan, snapshot, starts);
            ArtisanSubscription saved = artisanSubscriptions.save(subscription); subscriptionId = saved.getId();
            target.getArtisan().setPremium(true); response = toResponse(saved, SubscriberType.ARTISAN);
        } else {
            if (clientSubscriptions.countByAccountIdAndStatus(target.getId(), SubscriptionStatus.ACTIVE) > 0) throw new BadRequestException("Target already has an active subscription");
            ClientSubscription subscription = new ClientSubscription();
            populate(subscription, target, plan, snapshot, starts);
            ClientSubscription saved = clientSubscriptions.save(subscription); subscriptionId = saved.getId();
            target.getClient().setPremium(true); response = toResponse(saved, SubscriberType.CLIENT);
        }
        Payment payment = new Payment(); payment.setAccount(target); payment.setSubscriptionId(subscriptionId); payment.setProvider(PaymentProvider.CHARGILY);
        payment.setStatus(PaymentStatus.MANUALLY_GRANTED); payment.setManualGrant(true); payment.setAmount(plan.getAmount()); payment.setCurrency(plan.getCurrency());
        payment.setPlanSnapshot(snapshot); payment.setIdempotencyKey("manual-" + subscriptionId); payments.save(payment);
        auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_GRANTED, actor, target.getId(), "MANUAL_GRANT", "NONE", "ACTIVE", request.getReason(), payment.getId(), subscriptionId);
        notificationService.createForUser(target, "A subscription was manually granted to your account.", NotificationType.SUBSCRIPTION_MANUALLY_GRANTED, subscriptionId);
        return response;
    }

    @Transactional
    public void revoke(String subscriptionId, FinancialReasonRequest request) {
        User actor = currentUserProvider.requireCurrentUser();
        artisanSubscriptions.findWithLockById(subscriptionId).ifPresentOrElse(subscription -> {
            rules.requireTransition(subscription.getStatus(), SubscriptionStatus.REVOKED);
            String previous = subscription.getStatus().name(); subscription.setStatus(SubscriptionStatus.REVOKED);
            recordSubscriptionEvent(subscription.getAccount(), subscription.getId(), AnalyticsEvent.Subscription.REVOKED);
            cancelPendingPayments(subscriptionId);
            if (subscription.getAccount().getArtisan() != null) subscription.getAccount().getArtisan().setPremium(false);
            auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_REVOKED, actor, subscription.getAccount().getId(), "REVOKE", previous, "REVOKED", request.getReason(), null, subscriptionId);
            notificationService.createForUser(subscription.getAccount(), "Your subscription was revoked.", NotificationType.SUBSCRIPTION_REVOKED, subscriptionId);
        }, () -> clientSubscriptions.findWithLockById(subscriptionId).ifPresentOrElse(subscription -> {
            rules.requireTransition(subscription.getStatus(), SubscriptionStatus.REVOKED);
            String previous = subscription.getStatus().name(); subscription.setStatus(SubscriptionStatus.REVOKED);
            recordSubscriptionEvent(subscription.getAccount(), subscription.getId(), AnalyticsEvent.Subscription.REVOKED);
            cancelPendingPayments(subscriptionId);
            if (subscription.getAccount().getClient() != null) subscription.getAccount().getClient().setPremium(false);
            auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_REVOKED, actor, subscription.getAccount().getId(), "REVOKE", previous, "REVOKED", request.getReason(), null, subscriptionId);
            notificationService.createForUser(subscription.getAccount(), "Your subscription was revoked.", NotificationType.SUBSCRIPTION_REVOKED, subscriptionId);
        }, () -> { throw new ResourceNotFoundException("Subscription not found"); }));
    }

    @Transactional
    public void correctPayment(String paymentId, FinancialStateCorrectionRequest request) {
        User actor = currentUserProvider.requireCurrentUser();
        Payment payment = payments.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        PaymentStatus corrected;
        try {
            corrected = PaymentStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported payment status");
        }
        String previous = payment.getStatus().name();
        payment.setStatus(corrected);
        recordPaymentTransition(payment, previous, corrected.name());
        synchronizeCorrectedPayment(payment, corrected);
        auditLogService.logFinancialAction(AuditLogAction.PAYMENT_STATE_CORRECTED, actor, payment.getAccount().getId(), "STATE_CORRECTION", previous, corrected.name(), request.getReason(), payment.getId(), payment.getSubscriptionId());
    }

    private void synchronizeCorrectedPayment(Payment payment, PaymentStatus corrected) {
        if (corrected == PaymentStatus.PAID) {
            artisanSubscriptions.findWithLockById(payment.getSubscriptionId()).ifPresent(this::activateCorrectedArtisan);
            clientSubscriptions.findWithLockById(payment.getSubscriptionId()).ifPresent(this::activateCorrectedClient);
            return;
        }
        if (corrected != PaymentStatus.FAILED && corrected != PaymentStatus.CANCELED
                && corrected != PaymentStatus.EXPIRED) {
            return;
        }
        SubscriptionStatus terminalStatus = corrected == PaymentStatus.EXPIRED
                ? SubscriptionStatus.EXPIRED : SubscriptionStatus.CANCELED;
        artisanSubscriptions.findWithLockById(payment.getSubscriptionId())
                .ifPresent(subscription -> deactivateCorrectedArtisan(subscription, terminalStatus));
        clientSubscriptions.findWithLockById(payment.getSubscriptionId())
                .ifPresent(subscription -> deactivateCorrectedClient(subscription, terminalStatus));
    }

    private void activateCorrectedArtisan(ArtisanSubscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            LocalDateTime startsAt = LocalDateTime.now(clock);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartsAt(startsAt);
            subscription.setExpiresAt(rules.expiryFrom(startsAt, subscription.getBillingPeriod()));
        }
        syncArtisanPremium(subscription);
    }

    private void activateCorrectedClient(ClientSubscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            LocalDateTime startsAt = LocalDateTime.now(clock);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartsAt(startsAt);
            subscription.setExpiresAt(rules.expiryFrom(startsAt, subscription.getBillingPeriod()));
        }
        syncClientPremium(subscription);
    }

    private void deactivateCorrectedArtisan(ArtisanSubscription subscription, SubscriptionStatus terminalStatus) {
        if (subscription.getStatus() == SubscriptionStatus.PENDING || subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            subscription.setStatus(terminalStatus);
        }
        syncArtisanPremium(subscription);
    }

    private void deactivateCorrectedClient(ClientSubscription subscription, SubscriptionStatus terminalStatus) {
        if (subscription.getStatus() == SubscriptionStatus.PENDING || subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            subscription.setStatus(terminalStatus);
        }
        syncClientPremium(subscription);
    }

    @Transactional
    public void correctSubscription(String subscriptionId, FinancialStateCorrectionRequest request) {
        User actor = currentUserProvider.requireCurrentUser();
        SubscriptionStatus corrected;
        try {
            corrected = SubscriptionStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported subscription status");
        }
        if (artisanSubscriptions.findWithLockById(subscriptionId).isPresent()) {
            ArtisanSubscription subscription = artisanSubscriptions.findWithLockById(subscriptionId).orElseThrow();
            String previous = subscription.getStatus().name();
            subscription.setStatus(corrected);
            if (corrected == SubscriptionStatus.ACTIVE && subscription.getStartsAt() == null) {
                LocalDateTime startsAt = LocalDateTime.now(clock);
                subscription.setStartsAt(startsAt);
                subscription.setExpiresAt(rules.expiryFrom(startsAt, subscription.getBillingPeriod()));
            }
            syncArtisanPremium(subscription);
            auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_STATE_CORRECTED, actor, subscription.getAccount().getId(), "STATE_CORRECTION", previous, corrected.name(), request.getReason(), null, subscriptionId);
            return;
        }
        ClientSubscription subscription = clientSubscriptions.findWithLockById(subscriptionId).orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        String previous = subscription.getStatus().name();
        subscription.setStatus(corrected);
        if (corrected == SubscriptionStatus.ACTIVE && subscription.getStartsAt() == null) {
            LocalDateTime startsAt = LocalDateTime.now(clock);
            subscription.setStartsAt(startsAt);
            subscription.setExpiresAt(rules.expiryFrom(startsAt, subscription.getBillingPeriod()));
        }
        syncClientPremium(subscription);
        auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_STATE_CORRECTED, actor, subscription.getAccount().getId(), "STATE_CORRECTION", previous, corrected.name(), request.getReason(), null, subscriptionId);
    }

    @Transactional
    public void cancel(String subscriptionId, FinancialReasonRequest request) {
        User actor = currentUserProvider.requireCurrentUser();
        if (artisanSubscriptions.findWithLockById(subscriptionId).isPresent()) {
            ArtisanSubscription subscription = artisanSubscriptions.findWithLockById(subscriptionId).orElseThrow();
            String previous = subscription.getStatus().name();
            rules.requireTransition(subscription.getStatus(), SubscriptionStatus.CANCELED);
            subscription.setStatus(SubscriptionStatus.CANCELED);
            recordSubscriptionEvent(subscription.getAccount(), subscription.getId(), AnalyticsEvent.Subscription.CANCELED);
            cancelPendingPayments(subscriptionId);
            syncArtisanPremium(subscription);
            auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_CANCELED, actor, subscription.getAccount().getId(), "CANCEL", previous, "CANCELED", request.getReason(), null, subscriptionId);
            return;
        }
        ClientSubscription subscription = clientSubscriptions.findWithLockById(subscriptionId).orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        String previous = subscription.getStatus().name();
        rules.requireTransition(subscription.getStatus(), SubscriptionStatus.CANCELED);
        subscription.setStatus(SubscriptionStatus.CANCELED);
        recordSubscriptionEvent(subscription.getAccount(), subscription.getId(), AnalyticsEvent.Subscription.CANCELED);
        cancelPendingPayments(subscriptionId);
        syncClientPremium(subscription);
        auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_CANCELED, actor, subscription.getAccount().getId(), "CANCEL", previous, "CANCELED", request.getReason(), null, subscriptionId);
    }

    private void syncArtisanPremium(ArtisanSubscription subscription) {
        if (subscription.getAccount().getArtisan() != null) {
            subscription.getAccount().getArtisan().setPremium(subscription.getStatus() == SubscriptionStatus.ACTIVE);
        }
    }

    private void syncClientPremium(ClientSubscription subscription) {
        if (subscription.getAccount().getClient() != null) {
            subscription.getAccount().getClient().setPremium(subscription.getStatus() == SubscriptionStatus.ACTIVE);
        }
    }

    private void cancelPendingPayments(String subscriptionId) {
        payments.findBySubscriptionIdAndStatus(subscriptionId, PaymentStatus.PENDING)
                .forEach(payment -> payment.setStatus(PaymentStatus.CANCELED));
    }

    private void recordPaymentTransition(Payment payment, String previous, String current) {
        if (activityEventService != null && payment.getAccount() != null) {
            activityEventService.record(AnalyticsEvent.Payment.STATE_TRANSITION, payment.getAccount().getId(), payment.getId(),
                    Map.of("previousStatus", previous, "status", current, "source", "ADMIN_CORRECTION"));
        }
    }

    private void recordSubscriptionEvent(User account, String subscriptionId, AnalyticsEvent.Type eventType) {
        if (activityEventService != null && account != null) {
            activityEventService.record(eventType, account.getId(), subscriptionId,
                    Map.of("status", eventType.value().substring(AnalyticsEvent.Subscription.prefix().length()), "source", "ADMIN_ACTION"));
        }
    }

    private void populate(ArtisanSubscription subscription, User target, SubscriptionPlan plan, String snapshot, LocalDateTime starts) {
        subscription.setAccount(target); subscription.setStatus(SubscriptionStatus.ACTIVE); subscription.setPlanId(plan.getId()); subscription.setPlanName(plan.getName()); subscription.setBillingPeriod(plan.getBillingPeriod()); subscription.setAmount(plan.getAmount()); subscription.setCurrency(plan.getCurrency()); subscription.setEntitlementsSnapshot(snapshot); subscription.setStartsAt(starts); subscription.setExpiresAt(rules.expiryFrom(starts, plan.getBillingPeriod()));
    }
    private void populate(ClientSubscription subscription, User target, SubscriptionPlan plan, String snapshot, LocalDateTime starts) {
        subscription.setAccount(target); subscription.setStatus(SubscriptionStatus.ACTIVE); subscription.setPlanId(plan.getId()); subscription.setPlanName(plan.getName()); subscription.setBillingPeriod(plan.getBillingPeriod()); subscription.setAmount(plan.getAmount()); subscription.setCurrency(plan.getCurrency()); subscription.setEntitlementsSnapshot(snapshot); subscription.setStartsAt(starts); subscription.setExpiresAt(rules.expiryFrom(starts, plan.getBillingPeriod()));
    }
    private String snapshot(SubscriptionPlan plan) {
        Map<String, String> entitlements = plan.getEntitlements().stream().collect(LinkedHashMap::new, (map, item) -> map.put(item.getKey(), item.getValue()), LinkedHashMap::putAll);
        try { return objectMapper.writeValueAsString(SubscriptionPlanSnapshot.builder().planId(plan.getId()).name(plan.getName()).subscriberType(plan.getSubscriberType()).billingPeriod(plan.getBillingPeriod()).amount(plan.getAmount()).currency(plan.getCurrency()).entitlements(entitlements).build()); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Unable to snapshot plan", exception); }
    }
    private SubscriptionResponse toResponse(ArtisanSubscription value, SubscriberType type) { return SubscriptionResponse.builder().id(value.getId()).subscriberType(type).status(value.getStatus()).planName(value.getPlanName()).billingPeriod(value.getBillingPeriod()).amount(value.getAmount()).currency(value.getCurrency()).startsAt(value.getStartsAt()).expiresAt(value.getExpiresAt()).build(); }
    private SubscriptionResponse toResponse(ClientSubscription value, SubscriberType type) { return SubscriptionResponse.builder().id(value.getId()).subscriberType(type).status(value.getStatus()).planName(value.getPlanName()).billingPeriod(value.getBillingPeriod()).amount(value.getAmount()).currency(value.getCurrency()).startsAt(value.getStartsAt()).expiresAt(value.getExpiresAt()).build(); }
    public List<SubscriptionResponse> all(int limit, String query) {
        List<SubscriptionResponse> result = new ArrayList<>();
        artisanSubscriptions.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .forEach(value -> result.add(toResponse(value, SubscriberType.ARTISAN)));
        clientSubscriptions.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .forEach(value -> result.add(toResponse(value, SubscriberType.CLIENT)));
        String normalized = query == null ? "" : query.trim().toLowerCase();
        return result.stream().filter(value -> normalized.isBlank()
                        || value.getId().toLowerCase().contains(normalized)
                        || value.getPlanName().toLowerCase().contains(normalized)
                        || value.getStatus().name().toLowerCase().contains(normalized))
                .sorted(Comparator.comparing(SubscriptionResponse::getStartsAt, Comparator.nullsLast(Comparator.reverseOrder()))).limit(limit).toList();
    }
}
