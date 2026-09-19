package com.project.souklab.model.analytics;

import com.project.souklab.model.EnumValue;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Typed analytics report families with grouped compound report names. */
public final class AnalyticsReportType {
    private AnalyticsReportType() { }

    @JsonDeserialize(using = AnalyticsReportTypeValueDeserializer.class)
    public interface Key extends EnumValue {
        Family family();
    }

    public enum Family {
        OVERVIEW, GROWTH, ENGAGEMENT, MODERATION, CONTENT, SUBSCRIPTIONS,
        OPERATIONAL, TIME_SERIES, CSV
    }

    public static Key fromValue(String value) {
        for (Key key : all()) {
            if (key.value().equals(value)) return key;
        }
        throw new IllegalArgumentException("Unknown analytics report type: " + value);
    }

    private static Key[] all() {
        return new Key[]{
                Overview.REPORT, Growth.REPORT, Engagement.REPORT, Moderation.REPORT,
                Content.LEARNING, Subscriptions.PAYMENTS, Operational.REPORT,
                TimeSeries.REPORT, Csv.EXPORT
        };
    }

    public enum Overview implements Key {
        REPORT("OVERVIEW", Family.OVERVIEW);

        private final String value;
        private final Family family;

        Overview(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum Growth implements Key {
        REPORT("GROWTH", Family.GROWTH);

        private final String value;
        private final Family family;

        Growth(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum Engagement implements Key {
        REPORT("ENGAGEMENT", Family.ENGAGEMENT);

        private final String value;
        private final Family family;

        Engagement(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum Moderation implements Key {
        REPORT("MODERATION", Family.MODERATION);

        private final String value;
        private final Family family;

        Moderation(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum Content implements Key {
        LEARNING("CONTENT_LEARNING", Family.CONTENT);

        private final String value;
        private final Family family;

        Content(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum Subscriptions implements Key {
        PAYMENTS("SUBSCRIPTIONS_PAYMENTS", Family.SUBSCRIPTIONS);

        private final String value;
        private final Family family;

        Subscriptions(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum Operational implements Key {
        REPORT("OPERATIONAL", Family.OPERATIONAL);

        private final String value;
        private final Family family;

        Operational(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum TimeSeries implements Key {
        REPORT("TIME_SERIES", Family.TIME_SERIES);

        private final String value;
        private final Family family;

        TimeSeries(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }

    public enum Csv implements Key {
        EXPORT("CSV_EXPORT", Family.CSV);

        private final String value;
        private final Family family;

        Csv(String value, Family family) { this.value = value; this.family = family; }

        public Family family() { return family; }
        public String value() { return value; }
    }
}
