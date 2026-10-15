package com.project.souklab.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Confirmation for self-service account deletion.
 * Either a valid password (for password-based accounts) or an OAuth confirmation flag
 * (for OAuth-registered accounts with a recent login) is required.
 */
public record DeleteAccountRequest(
        String password,
        @JsonProperty("oauthConfirmed") Boolean providerConfirmed
) {
    public DeleteAccountRequest(String password) {
        this(password, null);
    }

    @JsonProperty("oauthConfirmed")
    public Boolean getOauthConfirmed() {
        return providerConfirmed;
    }
}
