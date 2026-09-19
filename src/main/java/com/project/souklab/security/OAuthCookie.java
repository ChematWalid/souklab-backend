package com.project.souklab.security;

import com.project.souklab.model.EnumValue;

/** Grouped names for application-owned OAuth cookies. */
public final class OAuthCookie {
    private OAuthCookie() {
    }

    public enum Intent implements EnumValue {
        NAME("SOUKLAB_OAUTH_INTENT");

        private final String value;

        Intent(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
