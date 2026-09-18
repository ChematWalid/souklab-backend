package com.project.souklab.service.subscription;

import com.project.souklab.dao.SubscriptionPlanRepository;
import com.project.souklab.dto.subscription.SubscriptionPlanResponse;
import com.project.souklab.model.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> listActivePlans() {
        return planRepository.findByActiveTrueOrderBySubscriberTypeAscBillingPeriodAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .subscriberType(plan.getSubscriberType())
                .name(plan.getName())
                .description(plan.getDescription())
                .billingPeriod(plan.getBillingPeriod())
                .amount(plan.getAmount())
                .currency(plan.getCurrency())
                .active(plan.isActive())
                .entitlements(plan.getEntitlements().stream().collect(
                        LinkedHashMap::new,
                        (map, entitlement) -> map.put(entitlement.getKey(), entitlement.getValue()),
                        LinkedHashMap::putAll))
                .build();
    }
}
