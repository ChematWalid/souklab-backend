package com.project.souklab.dto.directory;

import com.project.souklab.model.EnumValue;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/** Typed directory sort orders grouped by the field being ordered. */
public final class DirectorySortOrder {
    private DirectorySortOrder() { }

    @JsonDeserialize(using = DirectorySortOrderValueDeserializer.class)
    public interface Key extends EnumValue { }

    public static List<Key> all() {
        return List.of(Relevance.DEFAULT, Rating.DESC, Reviews.DESC, Views.DESC, Newest.FIRST);
    }

    public static Key fromValue(String value) {
        return all().stream()
                .filter(order -> order.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported directory sort order: " + value));
    }

    public enum Relevance implements Key {
        DEFAULT("RELEVANCE");
        private final String value;
        Relevance(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Rating implements Key {
        DESC("RATING_DESC");
        private final String value;
        Rating(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Reviews implements Key {
        DESC("REVIEWS_DESC");
        private final String value;
        Reviews(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Views implements Key {
        DESC("VIEWS_DESC");
        private final String value;
        Views(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Newest implements Key {
        FIRST("NEWEST");
        private final String value;
        Newest(String value) { this.value = value; }
        public String value() { return value; }
    }
}
