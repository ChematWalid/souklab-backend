package com.project.souklab.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;
import java.util.Optional;

public enum AccountRole implements EnumValue {
    ADMIN("ADMIN"),
    ARTISAN("ARTISAN"),
    CLIENT("CLIENT");

    private final String value;

    AccountRole(String value) { this.value = value; }

    public String value() { return value; }

    public static Optional<AccountRole> fromInput(String input) {
        if (input == null || input.isBlank()) return Optional.empty();
        String normalized = input.trim().toUpperCase(Locale.ROOT);
        for (AccountRole role : values()) {
            if (role.value.equals(normalized)) return Optional.of(role);
        }
        return Optional.empty();
    }

    @JsonCreator
    public static AccountRole fromJson(String input) {
        return fromInput(input).orElse(null);
    }
}
