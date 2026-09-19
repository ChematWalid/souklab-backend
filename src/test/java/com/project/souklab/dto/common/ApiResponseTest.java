package com.project.souklab.dto.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void createsSuccessResponses() {
        ApiResponse<String> withMessage = ApiResponse.success("data", "done");
        ApiResponse<String> defaultMessage = ApiResponse.success("data");
        ApiResponse<String> created = ApiResponse.created("data", "created");

        assertThat(withMessage.isSuccess()).isTrue();
        assertThat(withMessage.getMessage()).isEqualTo("done");
        assertThat(defaultMessage.getMessage()).isEqualTo("Success");
        assertThat(created.getCode()).isEqualTo(201);
    }

    @Test
    void createsErrorAndValidationResponses() {
        ApiResponse<Void> plain = ApiResponse.error("failed");
        ApiResponse<Void> coded = ApiResponse.error("ERR", "failed");
        Map<String, String> errors = Map.of("name", "required");
        ApiResponse<Void> validation = ApiResponse.validationError(errors);
        ApiResponse<Void> customValidation = ApiResponse.validationError("invalid", errors);

        assertThat(plain.isSuccess()).isFalse();
        assertThat(coded.getErrorCode()).isEqualTo("ERR");
        assertThat(validation.getMessage()).isEqualTo("Validation failed");
        assertThat(validation.getErrors()).containsEntry("name", "required");
        assertThat(customValidation.getMessage()).isEqualTo("invalid");
    }

    @Test
    void serializesTypedErrorCodeUsingStableWireValue() throws Exception {
        ApiResponse<Void> response = ApiResponse.error(ApiErrorCode.FORBIDDEN, "denied");

        String json = new ObjectMapper().writeValueAsString(response);

        assertThat(json).contains("\"errorCode\":\"FORBIDDEN\"");
    }
}
