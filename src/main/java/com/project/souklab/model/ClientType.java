package com.project.souklab.model;

import java.util.Locale;
import java.util.Optional;

public enum ClientType implements EnumValue {
    INDIVIDUAL("INDIVIDUAL"),
    BUSINESS("BUSINESS"),
    ENTERPRISE("ENTERPRISE");

    private final String value;

    ClientType(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    public static Optional<ClientType> fromInput(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = input.trim().toUpperCase(Locale.ROOT);
        for (ClientType type : values()) {
            if (type.value.equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
