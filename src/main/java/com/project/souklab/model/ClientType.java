package com.project.souklab.model;

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
}
