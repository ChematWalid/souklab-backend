package com.project.souklab.service.subscription;

import com.project.souklab.dao.SubscriptionPlanRepository;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dto.subscription.SubscriptionPlanRequest;
import com.project.souklab.dto.subscription.SubscriptionPlanResponse;
import com.project.souklab.model.SubscriptionPlan;
import com.project.souklab.model.SubscriptionPlanEntitlement;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.user.CurrentUserProvider;
import com.project.souklab.dto.subscription.FinancialReasonRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionPlanService {
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPlanRules rules;
    private final AppProperties appProperties;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> list() {
        return planRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public SubscriptionPlanResponse create(SubscriptionPlanRequest request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        apply(plan, request);
        SubscriptionPlan saved = planRepository.save(plan);
        auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_PLAN_CREATED, currentUserProvider.requireCurrentUser(), null,
                "PLAN_CREATE:" + saved.getId(), "NONE", saved.getName(), request.getReason(), null, null);
        return toResponse(saved);
    }

    @Transactional
    public SubscriptionPlanResponse update(String id, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Subscription plan not found"));
        String previous = plan.getName() + ":" + plan.getAmount() + ":" + plan.getCurrency();
        apply(plan, request);
        auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_PLAN_UPDATED, currentUserProvider.requireCurrentUser(), null,
                "PLAN_UPDATE:" + id, previous, plan.getName() + ":" + plan.getAmount() + ":" + plan.getCurrency(), request.getReason(), null, null);
        return toResponse(plan);
    }

    @Transactional
    public void deactivate(String id, FinancialReasonRequest request) {
        SubscriptionPlan plan = planRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Subscription plan not found"));
        String previous = Boolean.toString(plan.isActive());
        plan.setActive(false);
        auditLogService.logFinancialAction(AuditLogAction.SUBSCRIPTION_PLAN_DEACTIVATED, currentUserProvider.requireCurrentUser(), null,
                "PLAN_DEACTIVATE:" + id, previous, "false", request.getReason(), null, null);
    }

    private void apply(SubscriptionPlan plan, SubscriptionPlanRequest request) {
        rules.validateDzdAmount(request.getAmount());
        rules.validateCurrency(appProperties.getSubscription().getCurrency());
        plan.setSubscriberType(request.getSubscriberType());
        plan.setName(request.getName().trim());
        plan.setDescription(request.getDescription());
        plan.setBillingPeriod(request.getBillingPeriod());
        plan.setAmount(request.getAmount());
        plan.setCurrency(appProperties.getSubscription().getCurrency());
        plan.setActive(request.isActive());
        plan.getEntitlements().clear();
        if (request.getEntitlements() != null) {
            request.getEntitlements().forEach((key, value) -> {
                SubscriptionPlanEntitlement entitlement = new SubscriptionPlanEntitlement();
                entitlement.setPlan(plan);
                entitlement.setKey(key);
                entitlement.setValue(value);
                plan.getEntitlements().add(entitlement);
            });
        }
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder().id(plan.getId()).subscriberType(plan.getSubscriberType()).name(plan.getName())
                .description(plan.getDescription()).billingPeriod(plan.getBillingPeriod()).amount(plan.getAmount()).currency(plan.getCurrency())
                .active(plan.isActive())
                .entitlements(plan.getEntitlements().stream().collect(LinkedHashMap::new,
                        (map, entitlement) -> map.put(entitlement.getKey(), entitlement.getValue()), LinkedHashMap::putAll)).build();
    }
}
