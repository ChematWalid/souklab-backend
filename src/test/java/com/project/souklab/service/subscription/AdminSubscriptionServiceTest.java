package com.project.souklab.service.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanSubscriptionRepository;
import com.project.souklab.dao.ClientSubscriptionRepository;
import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dao.SubscriptionPlanRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.subscription.PaymentStateCorrectionRequest;
import com.project.souklab.model.Client;
import com.project.souklab.model.ClientSubscription;
import com.project.souklab.model.BillingPeriod;
import com.project.souklab.model.Payment;
import com.project.souklab.model.PaymentStatus;
import com.project.souklab.model.SubscriptionStatus;
import com.project.souklab.model.User;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.user.CurrentUserProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminSubscriptionServiceTest {
    @Test
    void paidPaymentCorrectionActivatesSubscriptionAndPremiumCompatibilityFlag() {
        CurrentUserProvider users = mock(CurrentUserProvider.class);
        PaymentRepository payments = mock(PaymentRepository.class);
        ArtisanSubscriptionRepository artisans = mock(ArtisanSubscriptionRepository.class);
        ClientSubscriptionRepository clients = mock(ClientSubscriptionRepository.class);
        SubscriptionPlanRules rules = mock(SubscriptionPlanRules.class);
        User account = new User();
        account.setId("account-1");
        Client client = new Client();
        account.setClient(client);
        Payment payment = new Payment();
        payment.setId("payment-1");
        payment.setAccount(account);
        payment.setSubscriptionId("subscription-1");
        payment.setStatus(PaymentStatus.PENDING);
        ClientSubscription subscription = new ClientSubscription();
        subscription.setAccount(account);
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setBillingPeriod(BillingPeriod.MONTHLY);
        when(users.requireCurrentUser()).thenReturn(new User());
        when(payments.findById("payment-1")).thenReturn(Optional.of(payment));
        when(artisans.findWithLockById("subscription-1")).thenReturn(Optional.empty());
        when(clients.findWithLockById("subscription-1")).thenReturn(Optional.of(subscription));
        when(rules.expiryFrom(any(LocalDateTime.class), any())).thenReturn(LocalDateTime.of(2026, 10, 18, 0, 0));

        AdminSubscriptionService service = new AdminSubscriptionService(users, mock(UserRepository.class),
                mock(SubscriptionPlanRepository.class), artisans, clients, payments, rules,
                mock(AuditLogService.class), mock(NotificationService.class), new ObjectMapper(),
                Clock.fixed(Instant.parse("2026-09-18T00:00:00Z"), ZoneOffset.UTC));
        PaymentStateCorrectionRequest request = new PaymentStateCorrectionRequest();
        request.setStatus(PaymentStatus.PAID);
        request.setReason("Verified bank settlement");

        service.correctPayment("payment-1", request);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getStartsAt()).isEqualTo(LocalDateTime.of(2026, 9, 18, 0, 0));
        assertThat(client.isPremium()).isTrue();
    }
}
