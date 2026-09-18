package com.project.souklab.service.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanSubscriptionRepository;
import com.project.souklab.dao.ClientSubscriptionRepository;
import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dao.SubscriptionPlanRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.dto.subscription.SubscriptionCheckoutRequest;
import com.project.souklab.integration.chargily.ChargilyCheckoutClient;
import com.project.souklab.model.Payment;
import com.project.souklab.model.User;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.user.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SubscriptionCheckoutServiceTest {
    private CurrentUserProvider currentUserProvider;
    private PaymentRepository paymentRepository;
    private UserRepository userRepository;
    private SubscriptionCheckoutService service;

    @BeforeEach
    void setUp() {
        currentUserProvider = mock(CurrentUserProvider.class);
        paymentRepository = mock(PaymentRepository.class);
        userRepository = mock(UserRepository.class);
        service = new SubscriptionCheckoutService(
                currentUserProvider,
                mock(SubscriptionPlanRepository.class),
                mock(ArtisanSubscriptionRepository.class),
                mock(ClientSubscriptionRepository.class),
                paymentRepository,
                mock(ChargilyCheckoutClient.class),
                mock(SubscriptionPlanRules.class),
                new AppProperties(),
                new ObjectMapper(),
                mock(NotificationService.class),
                userRepository);
    }

    @Test
    void requiresIdempotencyKeyBeforeAccessingAccountState() {
        assertThatThrownBy(() -> service.checkout(new SubscriptionCheckoutRequest(), " "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Idempotency-Key");
        verifyNoInteractions(currentUserProvider, paymentRepository);
    }

    @Test
    void rejectsIdempotencyKeyReuseForAnotherPlan() {
        User user = new User();
        user.setId("account-1");
        Payment existing = new Payment();
        existing.setPlanSnapshot("{\"planId\":\"plan-a\"}");
        existing.setId("payment-1");
        existing.setSubscriptionId("subscription-1");
        when(currentUserProvider.requireCurrentUser()).thenReturn(user);
        when(userRepository.findWithLockById("account-1")).thenReturn(Optional.of(user));
        when(paymentRepository.findByAccountIdAndIdempotencyKey("account-1", "key-1"))
                .thenReturn(Optional.of(existing));

        SubscriptionCheckoutRequest request = new SubscriptionCheckoutRequest();
        request.setPlanId("plan-b");
        assertThatThrownBy(() -> service.checkout(request, "key-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("another plan");
    }
}
