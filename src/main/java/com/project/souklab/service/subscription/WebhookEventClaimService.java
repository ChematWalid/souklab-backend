package com.project.souklab.service.subscription;

import com.project.souklab.dao.PaymentWebhookLogRepository;
import com.project.souklab.model.WebhookProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookEventClaimService {
    private final PaymentWebhookLogRepository repository;
    private final WebhookSecurityService securityService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(String providerEventId, ChargilyWebhookEvent.Checkout eventType, String checkoutId, byte[] rawBody) {
        String encryptedPayload = securityService.encrypt(new String(rawBody, StandardCharsets.UTF_8));
        return repository.claimEvent(UUID.randomUUID().toString(), providerEventId, eventType.value(), encryptedPayload,
                WebhookProcessingStatus.RECEIVED.value(), checkoutId) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquireProcessing(String providerEventId, LocalDateTime staleBefore) {
        return repository.acquireProcessing(providerEventId, staleBefore) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String providerEventId) {
        repository.markFailed(providerEventId, "Webhook processing failed");
    }
}
