package com.project.souklab.controller.subscription;

import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.dto.subscription.SubscriptionPlanResponse;
import com.project.souklab.model.BillingPeriod;
import com.project.souklab.model.SubscriberType;
import com.project.souklab.service.subscription.SubscriptionPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerSliceTest(controllers = SubscriptionPlanController.class)
class SubscriptionPlanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionPlanService planService;

    @Test
    void activePlansArePubliclyAvailable() throws Exception {
        when(planService.listActivePlans()).thenReturn(List.of(SubscriptionPlanResponse.builder()
                .id("plan-1").name("Monthly").subscriberType(SubscriberType.CLIENT)
                .billingPeriod(BillingPeriod.MONTHLY).amount(1000).currency("DZD").build()));

        mockMvc.perform(get("/api/v1/subscriptions/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("plan-1"))
                .andExpect(jsonPath("$.data[0].currency").value("DZD"));
    }

    @Test
    void activePlanByIdIsPubliclyAvailable() throws Exception {
        when(planService.getActivePlan("plan-1")).thenReturn(SubscriptionPlanResponse.builder()
                .id("plan-1").name("Monthly").subscriberType(SubscriberType.CLIENT)
                .billingPeriod(BillingPeriod.MONTHLY).amount(1000).currency("DZD").build());

        mockMvc.perform(get("/api/v1/subscriptions/plans/plan-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("plan-1"))
                .andExpect(jsonPath("$.data.currency").value("DZD"));
    }
}
