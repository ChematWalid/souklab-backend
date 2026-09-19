package com.project.souklab.security;

import com.project.souklab.model.EnumValue;

/** Grouped names for application-owned JWT claims. */
public final class JwtClaim {
    private JwtClaim() {
    }

    public enum Authorization implements EnumValue {
        VERSION("authz_version");

        private final String value;

        Authorization(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
