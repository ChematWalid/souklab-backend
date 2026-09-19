package com.project.souklab.model;

public enum OAuthProvider implements EnumValue {
    GOOGLE("GOOGLE"),
    GITHUB("GITHUB");

    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
