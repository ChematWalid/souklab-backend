package com.project.souklab.service.chat;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dto.chat.ChatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {
    private final SimpMessagingTemplate messagingTemplate;
    private final AppProperties properties;
    private final Clock clock;
    private final Map<String, AtomicInteger> sessions = new ConcurrentHashMap<>();

    @EventListener
    public void connected(SessionConnectedEvent event) { update(event.getMessage(), 1); }

    @EventListener
    public void disconnected(SessionDisconnectEvent event) { update(event.getMessage(), -1); }

    private void update(org.springframework.messaging.Message<?> message, int delta) {
        if (message == null) return;
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getUser() == null) return;
        String username = accessor.getUser().getName();
        AtomicInteger count = sessions.computeIfAbsent(username, ignored -> new AtomicInteger());
        int current = Math.max(0, count.addAndGet(delta));
        if (current == 0) sessions.remove(username, count);
        boolean online = current > 0;
        messagingTemplate.convertAndSend(properties.getChat().getPresenceDestination(), new ChatEvent(properties.getChat().getWebsocketProtocolVersion(), online ? "PRESENCE_ONLINE" : "PRESENCE_OFFLINE", null, null, null, LocalDateTime.now(clock), Map.of("username", username, "online", online)));
    }
}
