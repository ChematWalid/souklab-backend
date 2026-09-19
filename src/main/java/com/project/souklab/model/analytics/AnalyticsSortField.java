package com.project.souklab.model.analytics;

import com.project.souklab.analytics.AnalyticsMetric;
import com.project.souklab.model.EnumValue;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Typed analytics series fields grouped by their semantic dimension. */
public final class AnalyticsSortField {
    private AnalyticsSortField() { }

    @JsonDeserialize(using = AnalyticsSortFieldValueDeserializer.class)
    public interface Key extends EnumValue {
        AnalyticsMetric.Series seriesField();
    }

    public static Key fromField(String field) {
        for (Key key : all()) {
            if (key.value().equals(field)) return key;
        }
        throw new IllegalArgumentException("Unsupported analytics sort field: " + field);
    }

    private static Key[] all() {
        return new Key[]{Date.START, Date.END, Activity.EVENTS, Registration.NEW};
    }

    public enum Date implements Key {
        START(AnalyticsMetric.Series.START_DATE), END(AnalyticsMetric.Series.END_DATE);

        private final AnalyticsMetric.Series field;

        Date(AnalyticsMetric.Series field) { this.field = field; }

        @Override
        public AnalyticsMetric.Series seriesField() { return field; }

        @Override
        public String value() { return field.value(); }
    }

    public enum Activity implements Key {
        EVENTS(AnalyticsMetric.Series.ACTIVITY_EVENTS);

        private final AnalyticsMetric.Series field;

        Activity(AnalyticsMetric.Series field) { this.field = field; }

        @Override
        public AnalyticsMetric.Series seriesField() { return field; }

        @Override
        public String value() { return field.value(); }
    }

    public enum Registration implements Key {
        NEW(AnalyticsMetric.Series.NEW_REGISTRATIONS);

        private final AnalyticsMetric.Series field;

        Registration(AnalyticsMetric.Series field) { this.field = field; }

        @Override
        public AnalyticsMetric.Series seriesField() { return field; }

        @Override
        public String value() { return field.value(); }
    }
}
