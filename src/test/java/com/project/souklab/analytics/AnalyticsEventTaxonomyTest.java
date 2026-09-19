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
        assertThat(AnalyticsEvent.Formation.Moderation.APPROVED.value()).isEqualTo("FORMATION_MODERATION_APPROVED");
        assertThat(AnalyticsEvent.Feed.Post.PUBLISHED.value()).isEqualTo("FEED_POST_PUBLISHED");
        assertThat(AnalyticsEvent.Payment.State.TRANSITION.value()).isEqualTo("PAYMENT_STATE_TRANSITION");
        assertThat(AnalyticsEvent.Source.Payment.WEBHOOK.value()).isEqualTo("PAYMENT_WEBHOOK");
        assertThat(AnalyticsEvent.Source.Admin.CORRECTION.value()).isEqualTo("ADMIN_CORRECTION");
        assertThat(AnalyticsEvent.Subscription.RENEWAL.value()).isEqualTo("SUBSCRIPTION_RENEWAL");
        assertThat(AnalyticsEvent.Subscription.RENEWAL.status().value()).isEqualTo("RENEWAL");
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

    @Test
    void groupedMetricEnumsExposeStableApiKeys() {
        assertThat(AnalyticsMetric.Comparison.CURRENT.value()).isEqualTo("current");
        assertThat(AnalyticsMetric.Comparison.PREVIOUS.value()).isEqualTo("previous");
        assertThat(AnalyticsMetric.Historical.REGISTRATIONS.value()).isEqualTo("historical.registrations");
        assertThat(AnalyticsMetric.Summary.NEW_REGISTRATIONS.value()).isEqualTo("newRegistrations");
        assertThat(AnalyticsMetric.Series.NEW_REGISTRATIONS.value()).isEqualTo("newRegistrations");
        assertThat(AnalyticsMetric.EventRollup.SEPARATOR.value()).isEqualTo("\u0000");
        assertThat(AnalyticsMetric.Operational.REQUEST_COUNTER_METRIC.value()).isEqualTo("souklab.http.requests");
        assertThat(AnalyticsMetric.Operational.Metric.VIRUS_SCANS.value()).isEqualTo("souklab.virus.scans");
        assertThat(AnalyticsMetric.Operational.Dependency.RABBITMQ.value()).isEqualTo("rabbitmq");
        assertThat(AnalyticsMetric.Operational.Outcome.ERROR_REJECTED.value()).isEqualTo("error_rejected");
        assertThat(AnalyticsMetric.Operational.HttpMethod.fromValue("post"))
                .isEqualTo(AnalyticsMetric.Operational.HttpMethod.POST);
        assertThat(AnalyticsMetric.Operational.RequestOutcome.fromStatus(503))
                .isEqualTo(AnalyticsMetric.Operational.RequestOutcome.SERVER_ERROR_5XX);
    }

    @Test
    void groupedMetadataEnumsExposeStableEventKeys() {
        assertThat(AnalyticsMetadata.State.STATUS.value()).isEqualTo("status");
        assertThat(AnalyticsMetadata.Subscription.SOURCE.value()).isEqualTo("source");
        assertThat(AnalyticsMetadata.Payment.PROVIDER_EVENT.value()).isEqualTo("providerEvent");
        assertThat(AnalyticsMetadata.Payment.ID.value()).isEqualTo("payment_id");
        assertThat(AnalyticsMetadata.Provider.SUBSCRIPTION_ID.value()).isEqualTo("subscription_id");
    }
}
