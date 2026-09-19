package com.project.souklab.model;

/** Stable string representation for enum values crossing a persistence or API boundary. */
public interface EnumValue {

    default String value() {
        return toString();
    }
}
