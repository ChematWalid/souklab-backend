package com.project.souklab.controller.subscription;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.subscription.PaymentResponse;
import com.project.souklab.dto.subscription.SubscriptionResponse;
import com.project.souklab.service.subscription.SubscriptionAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Subscription Account", description = "Current authenticated user subscription state, cancellation, and payment transaction history")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SubscriptionAccountController {
    private final SubscriptionAccountService accountService;

    @GetMapping("/subscriptions/current")
    @Operation(summary = "Get current subscription", description = "Retrieves active subscription details, renewal dates, and tier entitlements for the authenticated user.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> current() { return ResponseEntity.ok(ApiResponse.success(accountService.current())); }

    @GetMapping("/subscriptions")
    @Operation(summary = "Get subscription history", description = "Retrieves complete subscription history and billing cycles for the authenticated user.")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> history() { return ResponseEntity.ok(ApiResponse.success(accountService.history())); }

    @PostMapping("/subscriptions/{id}/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancels auto-renewal on an active subscription by ID.")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable String id) { accountService.cancel(id); return ResponseEntity.ok(ApiResponse.success(null, "Subscription canceled")); }

    @GetMapping("/payments")
    @Operation(summary = "Get payment history", description = "Lists payment transactions and checkout receipts for the authenticated user.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> payments() { return ResponseEntity.ok(ApiResponse.success(accountService.payments())); }

    @GetMapping("/payments/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details and receipt for a specific payment ID.")
    public ResponseEntity<ApiResponse<PaymentResponse>> payment(@PathVariable String id) { return ResponseEntity.ok(ApiResponse.success(accountService.payment(id))); }
}
