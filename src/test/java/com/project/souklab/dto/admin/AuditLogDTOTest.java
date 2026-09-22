package com.project.souklab.dto.admin;

import com.project.souklab.model.AuditLog;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogDTOTest {

    @Test
    void mapsAuditLogWithAndWithoutAttribution() {
        assertThat(AuditLogDTO.from(null)).isNull();

        User user = User.builder().email("admin@example.com").build();
        user.setId("user-1");
        AuditLog attributed = new AuditLog(AuditLogAction.User.APPROVED, "approved", user);
        attributed.setId("log-1");
        attributed.setTargetAccountId("target-1");
        attributed.setOperation("MANUAL_GRANT");
        attributed.setPreviousState("NONE");
        attributed.setNewState("ACTIVE");
        attributed.setReason("approved");
        attributed.setPaymentId("payment-1");
        attributed.setSubscriptionId("subscription-1");

        AuditLogDTO result = AuditLogDTO.from(attributed);
        assertThat(result.getId()).isEqualTo("log-1");
        assertThat(result.getAction()).isEqualTo(AuditLogAction.User.APPROVED);
        assertThat(result.getDetails()).isEqualTo("approved");
        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getUserEmail()).isEqualTo("admin@example.com");
        assertThat(result.getTargetAccountId()).isEqualTo("target-1");
        assertThat(result.getOperation()).isEqualTo("MANUAL_GRANT");
        assertThat(result.getPreviousState()).isEqualTo("NONE");
        assertThat(result.getNewState()).isEqualTo("ACTIVE");
        assertThat(result.getReason()).isEqualTo("approved");
        assertThat(result.getPaymentId()).isEqualTo("payment-1");
        assertThat(result.getSubscriptionId()).isEqualTo("subscription-1");

        AuditLog unattributed = new AuditLog(AuditLogAction.Report.DISMISSED, null, null);
        assertThat(AuditLogDTO.from(unattributed).getUserId()).isNull();
        assertThat(AuditLogDTO.from(unattributed).getUserEmail()).isNull();
    }
}
