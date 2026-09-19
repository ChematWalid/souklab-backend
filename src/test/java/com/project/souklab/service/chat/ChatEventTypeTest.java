package com.project.souklab.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.dto.chat.ChatEvent;
import com.project.souklab.dto.chat.ChatEventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatEventTypeTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesTypedEventUsingStableWireValue() throws Exception {
        ChatEvent event = ChatEvent.create("v1", ChatEventType.Message.CREATED,
                "conversation-1", "message-1", null, null, null);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(event));

        assertThat(json.path("type").asText()).isEqualTo("MESSAGE_CREATED");
    }
}
