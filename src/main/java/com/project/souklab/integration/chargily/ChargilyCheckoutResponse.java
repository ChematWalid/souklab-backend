package com.project.souklab.integration.chargily;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChargilyCheckoutResponse {
    private String id;
    private String checkoutUrl;
    private String customerId;
    private String invoiceId;
}
