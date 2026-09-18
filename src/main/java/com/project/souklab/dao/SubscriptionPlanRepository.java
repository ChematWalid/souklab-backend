package com.project.souklab.dao;

import com.project.souklab.model.SubscriberType;
import com.project.souklab.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    List<SubscriptionPlan> findByActiveTrueOrderBySubscriberTypeAscBillingPeriodAsc();
    List<SubscriptionPlan> findBySubscriberTypeAndActiveTrue(SubscriberType subscriberType);
}
