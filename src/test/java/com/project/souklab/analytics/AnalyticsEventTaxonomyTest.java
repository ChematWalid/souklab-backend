package com.project.souklab.analytics;

import com.project.souklab.security.Permission;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.analytics.AnalyticsFilterKey;
import com.project.souklab.model.analytics.AnalyticsSortField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsEventTaxonomyTest {
    @Test
    void groupedEventEnumsExposeStablePersistedValues() {
        assertThat(AnalyticsEvent.Report.RESOLVED.value()).isEqualTo("REPORT_RESOLVED");
        assertThat(AnalyticsEvent.Authentication.Login.SUCCEEDED.value()).isEqualTo("LOGIN_SUCCEEDED");
        assertThat(AnalyticsEvent.Subscription.RENEWAL.value()).isEqualTo("SUBSCRIPTION_RENEWAL");
        assertThat(AnalyticsEvent.fromValue("REPORT_RESOLVED")).contains(AnalyticsEvent.Report.RESOLVED);
        assertThat(AnalyticsEvent.fromValue("unknown_event")).isEmpty();
        assertThat(AnalyticsFilterKey.fromKey("eventType")).contains(AnalyticsFilterKey.EVENT_TYPE);
        assertThat(AnalyticsFilterKey.fromKey("unknown")).isEmpty();
        assertThat(AnalyticsSortField.fromField("activityEvents")).isEqualTo(AnalyticsSortField.ACTIVITY_EVENTS);
        assertThat(EnrollmentStatus.ATTENDED.value()).isEqualTo("ATTENDED");
    }

    @Test
    void groupedPermissionEnumsExposeStableAuthorities() {
        assertThat(Permission.Admin.USERS.value()).isEqualTo("permission:admin:users");
        assertThat(Permission.Analytics.ADMIN.value()).isEqualTo("permission:analytics:admin");
        assertThat(Permission.Financial.ADMIN.value()).isEqualTo("permission:financial:admin");
    }
}
