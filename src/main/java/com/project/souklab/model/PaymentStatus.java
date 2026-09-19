package com.project.souklab.model;

public enum PaymentStatus implements EnumValue {
    CREATED,
    PENDING,
    PAID,
    FAILED,
    CANCELED,
    EXPIRED,
    MANUALLY_GRANTED
}
