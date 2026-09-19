package com.project.souklab.service.subscription;

import com.project.souklab.dao.PaymentRepository;
import com.project.souklab.dto.subscription.FinancialReasonRequest;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.Payment;
import com.project.souklab.model.NotificationType;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRefundService {
    private final PaymentRepository paymentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Transactional(noRollbackFor = BadRequestException.class)
    public void reject(Payment payment, FinancialReasonRequest request) {
        auditLogService.logFinancialAction(AuditLogAction.REFUND_REQUEST_REJECTED,
                currentUserProvider.requireCurrentUser(), payment.getAccount().getId(), "REFUND_REQUEST",
                payment.getStatus().value(), payment.getStatus().value(), request.getReason(), payment.getId(), payment.getSubscriptionId());
        notificationService.createForUser(payment.getAccount(),
                "Refund requests are unavailable for Chargily Pay V2.", NotificationType.REFUND_REQUEST_UNAVAILABLE, payment.getId());
        throw new BadRequestException("Chargily Pay V2 refunds are not supported");
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public void reject(String paymentId, FinancialReasonRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        reject(payment, request);
    }
}
