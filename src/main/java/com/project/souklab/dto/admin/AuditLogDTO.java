package com.project.souklab.dto.admin;

import com.project.souklab.model.AuditLog;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.AuditLogActionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private String id;
    @JsonDeserialize(using = AuditLogActionDeserializer.class)
    private AuditLogAction.Key action;
    private String details;
    private String userEmail;
    private String userId;
    private String targetAccountId;
    private String operation;
    private String previousState;
    private String newState;
    private String reason;
    private String paymentId;
    private String subscriptionId;
    private LocalDateTime createdAt;

    public static AuditLogDTO from(AuditLog entity) {
        if (entity == null) {
            return null;
        }
        return AuditLogDTO.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .details(entity.getDetails())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .targetAccountId(entity.getTargetAccountId())
                .operation(entity.getOperation())
                .previousState(entity.getPreviousState())
                .newState(entity.getNewState())
                .reason(entity.getReason())
                .paymentId(entity.getPaymentId())
                .subscriptionId(entity.getSubscriptionId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
