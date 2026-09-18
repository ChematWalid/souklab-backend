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

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SubscriptionAccountController {
    private final SubscriptionAccountService accountService;

    @GetMapping("/subscriptions/current")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> current() { return ResponseEntity.ok(ApiResponse.success(accountService.current())); }
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> history() { return ResponseEntity.ok(ApiResponse.success(accountService.history())); }
    @PostMapping("/subscriptions/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable String id) { accountService.cancel(id); return ResponseEntity.ok(ApiResponse.success(null, "Subscription canceled")); }
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> payments() { return ResponseEntity.ok(ApiResponse.success(accountService.payments())); }
    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> payment(@PathVariable String id) { return ResponseEntity.ok(ApiResponse.success(accountService.payment(id))); }
}
