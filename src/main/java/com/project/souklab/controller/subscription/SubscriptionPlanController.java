package com.project.souklab.controller.subscription;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.subscription.SubscriptionPlanResponse;
import com.project.souklab.service.subscription.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionPlanController {
    private final SubscriptionPlanService planService;

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listPlans() {
        return ResponseEntity.ok(ApiResponse.success(planService.listActivePlans()));
    }
}
