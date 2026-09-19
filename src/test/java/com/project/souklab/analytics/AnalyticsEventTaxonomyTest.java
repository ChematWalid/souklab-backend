package com.project.souklab.analytics;

import com.project.souklab.security.Permission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsEventTaxonomyTest {
    @Test
    void groupedEventEnumsExposeStablePersistedValues() {
        assertThat(AnalyticsEvent.Report.RESOLVED.value()).isEqualTo("REPORT_RESOLVED");
        assertThat(AnalyticsEvent.Authentication.LOGIN_SUCCEEDED.value()).isEqualTo("LOGIN_SUCCEEDED");
        assertThat(AnalyticsEvent.Subscription.RENEWAL.value()).isEqualTo("SUBSCRIPTION_RENEWAL");
    }

    @Test
    void groupedPermissionEnumsExposeStableAuthorities() {
        assertThat(Permission.Admin.USERS.authority()).isEqualTo("permission:admin:users");
        assertThat(Permission.Analytics.ADMIN.authority()).isEqualTo("permission:analytics:admin");
        assertThat(Permission.Financial.ADMIN.authority()).isEqualTo("permission:financial:admin");
    }
}
