package com.project.souklab.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** Stable string representation for enum values crossing a persistence or API boundary. */
public interface EnumValue {

    @JsonValue
    default String value() {
        return toString();
    }
}
