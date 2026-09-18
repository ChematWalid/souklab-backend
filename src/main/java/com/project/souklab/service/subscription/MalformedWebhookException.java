package com.project.souklab.service.subscription;

public class MalformedWebhookException extends RuntimeException {
    public MalformedWebhookException(String message) {
        super(message);
    }
}
