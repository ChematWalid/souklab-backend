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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription Checkout", description = "Initiate subscription checkout and renewals via Chargily Pay V2 (EDAHABIA / CIB)")
@RequiredArgsConstructor
public class SubscriptionCheckoutController {
    private final SubscriptionCheckoutService checkoutService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Checkout subscription", description = "Creates a Chargily Pay V2 checkout session for a selected subscription plan. Returns the EDAHABIA/CIB checkout URL to redirect the user to. Supports Idempotency-Key header.")
    public ResponseEntity<ApiResponse<SubscriptionCheckoutResponse>> checkout(
            @Valid @RequestBody SubscriptionCheckoutRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(ApiResponse.success(checkoutService.checkout(request, idempotencyKey)));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Renew subscription", description = "Creates a Chargily Pay renewal checkout session for an existing subscription. Supports Idempotency-Key header.")
    public ResponseEntity<ApiResponse<SubscriptionCheckoutResponse>> renew(
            @PathVariable String id,
            @Valid @RequestBody SubscriptionCheckoutRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(ApiResponse.success(checkoutService.renew(id, request, idempotencyKey)));
    }
}
