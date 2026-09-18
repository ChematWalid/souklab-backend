package com.project.souklab.controller.subscription;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.subscription.SubscriptionCheckoutRequest;
import com.project.souklab.dto.subscription.SubscriptionCheckoutResponse;
import com.project.souklab.service.subscription.SubscriptionCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionCheckoutController {
    private final SubscriptionCheckoutService checkoutService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionCheckoutResponse>> checkout(
            @Valid @RequestBody SubscriptionCheckoutRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(ApiResponse.success(checkoutService.checkout(request, idempotencyKey)));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<ApiResponse<SubscriptionCheckoutResponse>> renew(
            @PathVariable String id,
            @Valid @RequestBody SubscriptionCheckoutRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(ApiResponse.success(checkoutService.renew(id, request, idempotencyKey)));
    }
}
