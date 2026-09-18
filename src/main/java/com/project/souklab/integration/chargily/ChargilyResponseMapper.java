package com.project.souklab.integration.chargily;

import com.fasterxml.jackson.databind.JsonNode;

public class ChargilyResponseMapper {
    public ChargilyCheckoutResponse toCheckoutResponse(JsonNode body) {
        ChargilyCheckoutResponse response = new ChargilyCheckoutResponse();
        response.setId(text(body, "id"));
        response.setCheckoutUrl(text(body, "checkout_url", "checkoutUrl", "url"));
        response.setCustomerId(text(body, "customer_id", "customerId"));
        response.setInvoiceId(text(body, "invoice_id", "invoiceId"));
        if (response.getId() == null || response.getCheckoutUrl() == null) {
            throw new ChargilyProviderException("Provider response omitted checkout identifiers");
        }
        return response;
    }

    private String text(JsonNode body, String... fields) {
        for (String field : fields) {
            JsonNode value = body.get(field);
            if (value != null && value.isTextual() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }
}
