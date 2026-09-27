package com.project.souklab.security;

import com.project.souklab.dto.auth.JwtResponseDTO;

/** Single-use store for short-lived OAuth callback credentials. */
public interface AuthorizationCodeStore {
    String put(JwtResponseDTO response);
    JwtResponseDTO consume(String code);
}
