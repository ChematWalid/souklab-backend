package com.project.souklab.security;

import com.project.souklab.model.EnumValue;
import org.springframework.security.core.GrantedAuthority;

/** Typed authorities that are supplied by the security framework rather than application permissions. */
public interface SecurityAuthority extends EnumValue, GrantedAuthority {

    @Override
    default String getAuthority() {
        return value();
    }

    enum Anonymous implements SecurityAuthority {
        ROLE("ROLE_ANONYMOUS");

        private final String value;

        Anonymous(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
