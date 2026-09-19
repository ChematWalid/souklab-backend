package com.project.souklab.model.analytics;

import com.project.souklab.model.EnumValue;

public enum OutboxStatus implements EnumValue {
    PENDING,
    PUBLISHED,
    DEAD_LETTER
}
