package com.project.souklab.model;

/** Supported ISO currency codes for platform billing. */
public enum CurrencyCode implements EnumValue {
    DZD;

    public boolean matches(String candidate) {
        return value().equals(candidate);
    }
}
