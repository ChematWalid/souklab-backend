package com.project.souklab.service.subscription;

import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dto.subscription.FinancialReasonRequest;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.model.Payment;
import com.project.souklab.model.PaymentStatus;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.FinancialAuditOperation;
import com.project.souklab.model.User;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.user.CurrentUserProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;

class AdminRefundServiceTest {
    @Test
    void rejectedRefundIsAuditedAndNotifiedBeforeUnsupportedError() {
        PaymentRepository payments = mock(PaymentRepository.class);
        CurrentUserProvider users = mock(CurrentUserProvider.class);
        AuditLogService audit = mock(AuditLogService.class);
        NotificationService notifications = mock(NotificationService.class);
        AdminRefundService service = new AdminRefundService(payments, users, audit, notifications);
        Payment payment = new Payment();
        payment.setId("payment-1");
        payment.setSubscriptionId("subscription-1");
        payment.setStatus(PaymentStatus.PAID);
        User account = new User();
        account.setId("account-1");
        payment.setAccount(account);
        User actor = new User();
        when(users.requireCurrentUser()).thenReturn(actor);
        when(payments.findById("payment-1")).thenReturn(Optional.of(payment));
        FinancialReasonRequest request = new FinancialReasonRequest();
        request.setReason("Provider refund contract unavailable");

        assertThatThrownBy(() -> service.reject("payment-1", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("refunds are not supported");
        verify(audit).logFinancialAction(any(), any(), any(), any(FinancialAuditOperation.Type.class),
                any(), any(), any(), any(), any());
        verify(notifications).createForUser(account, "Refund requests are unavailable for Chargily Pay V2.",
                NotificationType.REFUND_REQUEST_UNAVAILABLE, "payment-1");
    }
}
