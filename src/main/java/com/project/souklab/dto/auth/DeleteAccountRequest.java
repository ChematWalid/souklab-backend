package com.project.souklab.dto.auth;

/** Confirmation for self-service account deletion. One of the two confirmations is required. */
public record DeleteAccountRequest(String password, Boolean oauthConfirmed) { }
