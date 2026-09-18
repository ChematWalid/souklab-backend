package com.project.souklab.dto.subscription;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubscriptionCheckoutResponse {
    String paymentId;
    String subscriptionId;
    String providerCheckoutId;
    String checkoutUrl;
}
