package com.project.souklab.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.dto.chat.ChatEvent;
import com.project.souklab.dto.chat.ChatEventType;
import com.project.souklab.dto.chat.ChatMetadata;

import java.util.Map;
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

    @Test
    void serializesTypedMetadataUsingStableWireKeys() throws Exception {
        ChatEvent event = ChatEvent.create("v1", ChatEventType.Presence.ONLINE,
                null, null, null, null,
                Map.of(ChatMetadata.Presence.USERNAME, "user@example.com",
                        ChatMetadata.Presence.ONLINE, true));

        JsonNode payload = objectMapper.readTree(objectMapper.writeValueAsString(event)).path("payload");

        assertThat(payload.path("username").asText()).isEqualTo("user@example.com");
        assertThat(payload.path("online").asBoolean()).isTrue();
    }
}
