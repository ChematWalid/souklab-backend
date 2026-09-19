package com.project.souklab.service.chat;
import java.util.Map;
import org.springframework.messaging.MessageHeaders;

import com.project.souklab.dto.chat.ChatEvent;

import com.project.souklab.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.mockito.Mockito.*;
import java.time.Clock;

class ChatPresenceServiceTest {
    @Test
    void lifecycleEventsWithoutAuthenticatedPrincipalAreIgnored() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        ChatPresenceService service = new ChatPresenceService(template, new AppProperties(), Clock.systemUTC());
        SessionConnectedEvent connected = mock(SessionConnectedEvent.class);
        SessionDisconnectEvent disconnected = mock(SessionDisconnectEvent.class);
        when(connected.getMessage()).thenReturn(null);
        when(disconnected.getMessage()).thenReturn(null);
        service.connected(connected);
        service.disconnected(disconnected);
        verifyNoInteractions(template);
    }

    @Test
    void emitsOnlineForFirstSessionAndOfflineAfterLastSession() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        AppProperties properties = new AppProperties();
        properties.getChat().setPresenceDestination("/topic/presence");
        properties.getChat().setWebsocketProtocolVersion("v1");
        ChatPresenceService service = new ChatPresenceService(template, properties, Clock.systemUTC());
        Message<byte[]> message = userMessage("user@test");
        SessionConnectedEvent connected = mock(SessionConnectedEvent.class);
        SessionDisconnectEvent disconnected = mock(SessionDisconnectEvent.class);
        when(connected.getMessage()).thenReturn(message);
        when(disconnected.getMessage()).thenReturn(message);

        service.connected(connected);
        service.disconnected(disconnected);

        verify(template, times(2)).convertAndSend(eq("/topic/presence"), any(ChatEvent.class));
    }

    @Test
    void messageWithoutUserIsIgnored() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        ChatPresenceService service = new ChatPresenceService(template, new AppProperties(), Clock.systemUTC());
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
        service.connected(new SessionConnectedEvent(this, MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders())));
        verifyNoInteractions(template);
    }

    @Test
    void messageWithoutStompHeadersIsIgnored() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        ChatPresenceService service = new ChatPresenceService(template, new AppProperties(), Clock.systemUTC());
        service.connected(new SessionConnectedEvent(this, MessageBuilder.createMessage(new byte[0], new MessageHeaders(Map.of()))));
        verifyNoInteractions(template);
    }

    private Message<byte[]> userMessage(String username) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
        accessor.setUser(() -> username);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
