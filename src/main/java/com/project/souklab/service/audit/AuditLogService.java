package com.project.souklab.service.audit;

import com.project.souklab.dao.AuditLogRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AuditLog;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.FinancialAuditOperation;
import com.project.souklab.model.User;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Records an audit log entry for a specific action performed by a user.
     * Resolves the authenticated user's username on the calling thread before
     * delegating to the persistence worker. If no user is authenticated,
     * the action is logged under 'anonymousUser'.
     *
     * @param action the {@link AuditLogAction} representing the action type
     * @param details a detailed description of the action and its context
     */
    public void logAction(AuditLogAction action, String details) {
        String username = SecurityUtils.getCurrentUsername();
        String effectiveUsername = (username != null && !username.equals(ANONYMOUS_USER)) ? username : ANONYMOUS_USER;
        recordAuditLog(action, details, effectiveUsername);
    }

    /**
     * Internal async worker to persist the audit log.
     *
     * @param action the {@link AuditLogAction} representing the action type
     * @param details a detailed description of the action
     * @param username the username associated with this action
     */
    @Async("applicationTaskExecutor")
    @Transactional
    public void logAction(AuditLogAction action, String details, String username) {
        recordAuditLog(action, details, username);
    }

    @Transactional
    public void logFinancialAction(AuditLogAction action, User actor, String targetAccountId, FinancialAuditOperation.Type operation,
                                   String previousState, String newState, String reason,
                                   String paymentId, String subscriptionId) {
        logFinancialAction(action, actor, targetAccountId, operation.value(), previousState, newState,
                reason, paymentId, subscriptionId);
    }

    public void logFinancialAction(AuditLogAction action, User actor, String targetAccountId, String operation,
                                   String previousState, String newState, String reason,
                                   String paymentId, String subscriptionId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUser(actor);
        auditLog.setTargetAccountId(targetAccountId);
        auditLog.setOperation(operation);
        auditLog.setPreviousState(previousState);
        auditLog.setNewState(newState);
        auditLog.setReason(reason);
        auditLog.setPaymentId(paymentId);
        auditLog.setSubscriptionId(subscriptionId);
        auditLogRepository.save(auditLog);
    }

    private void recordAuditLog(AuditLogAction action, String details, String username) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setDetails(details);

            if (username != null && !username.equals(ANONYMOUS_USER)) {
                userRepository.findByEmail(username).ifPresent(auditLog::setUser);
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to record audit log entry for action: {}", action, e);
        }
    }
}
