package com.project.souklab.controller.subscription;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.subscription.SubscriptionPlanRequest;
import com.project.souklab.dto.subscription.SubscriptionPlanResponse;
import com.project.souklab.dto.subscription.FinancialReasonRequest;
import com.project.souklab.service.subscription.AdminSubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/subscription-plans")
@RequiredArgsConstructor
@PreAuthorize("@accessControl.canManageFinancialOperations(authentication)")
public class AdminSubscriptionPlanController {
    private final AdminSubscriptionPlanService planService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(planService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(planService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> create(@Valid @RequestBody SubscriptionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.created(planService.create(request), "Subscription plan created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> update(@PathVariable String id,
                                                                         @Valid @RequestBody SubscriptionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(planService.update(id, request), "Subscription plan updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String id, @Valid @RequestBody FinancialReasonRequest request) {
        planService.deactivate(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription plan deactivated"));
    }
}
