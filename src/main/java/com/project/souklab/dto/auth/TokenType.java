package com.project.souklab.dto.auth;

import com.project.souklab.model.EnumValue;

/** Supported authentication token schemes exposed by the API. */
public enum TokenType implements EnumValue {
    BEARER("Bearer");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
