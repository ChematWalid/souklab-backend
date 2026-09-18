package com.project.souklab.config;

import com.project.souklab.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {
    @Mock JwtUtils jwtUtils;
    @Mock UserDetailsService userDetailsService;
    @Mock UserDetails userDetails;

    @Test
    void preSend_returnsNonConnectMessagesUnchanged() {
        WebSocketAuthInterceptor interceptor = interceptor();
        Message<String> message = MessageBuilder.withPayload("payload").build();

        assertThat(interceptor.preSend(message, null)).isSameAs(message);
        StompHeaderAccessor sendAccessor = StompHeaderAccessor.create(StompCommand.SEND);
        sendAccessor.setLeaveMutable(true);
        Message<byte[]> sendMessage = MessageBuilder.createMessage(new byte[0], sendAccessor.getMessageHeaders());
        assertThat(interceptor.preSend(sendMessage, null)).isSameAs(sendMessage);
        verifyNoInteractions(jwtUtils, userDetailsService);
    }

    @Test
    void preSend_authenticatesValidBearerTokenAndSetsPrincipal() {
        when(jwtUtils.validateJwtToken("token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("token")).thenReturn("client@example.com");
        when(userDetailsService.loadUserByUsername("client@example.com")).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("client@example.com");
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);

        Message<?> result = interceptor().preSend(connect("Bearer token"), null);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("client@example.com");
        assertThat(((Authentication) accessor.getUser()).getAuthorities())
                .isEqualTo(userDetails.getAuthorities());
    }

    @Test
    void preSend_rejectsMissingInvalidAndUnusableCredentials() {
        WebSocketAuthInterceptor interceptor = interceptor();

        assertThatThrownBy(() -> interceptor.preSend(connect(null), null))
                .isInstanceOf(AccessDeniedException.class);

        when(jwtUtils.validateJwtToken("bad-token")).thenReturn(false);
        assertThatThrownBy(() -> interceptor.preSend(connect("bad-token"), null))
                .isInstanceOf(AccessDeniedException.class);

        when(jwtUtils.validateJwtToken("disabled-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("disabled-token")).thenReturn("disabled@example.com");
        when(userDetailsService.loadUserByUsername("disabled@example.com")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(false);
        assertThatThrownBy(() -> interceptor.preSend(connect("Bearer disabled-token"), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void preSend_rejectsEmptyHeaderAndLockedOrInvalidTokenFailures() throws Exception {
        WebSocketAuthInterceptor interceptor = interceptor();
        StompHeaderAccessor emptyHeaders = StompHeaderAccessor.create(StompCommand.CONNECT);
        emptyHeaders.addNativeHeader("Authorization", "placeholder");
        var nativeHeaders = StompHeaderAccessor.class.getDeclaredMethod("getNativeHeaders");
        nativeHeaders.setAccessible(true);
        @SuppressWarnings("unchecked")
        var headers = (java.util.Map<String, java.util.List<String>>) nativeHeaders.invoke(emptyHeaders);
        headers.put("Authorization", java.util.List.of());
        emptyHeaders.setLeaveMutable(true);
        Message<byte[]> emptyHeaderMessage = MessageBuilder.createMessage(new byte[0], emptyHeaders.getMessageHeaders());
        assertThatThrownBy(() -> interceptor.preSend(emptyHeaderMessage, null))
                .isInstanceOf(AccessDeniedException.class);

        when(jwtUtils.validateJwtToken("locked-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("locked-token")).thenReturn("locked@example.com");
        when(userDetailsService.loadUserByUsername("locked@example.com")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(false);
        assertThatThrownBy(() -> interceptor.preSend(connect("Bearer locked-token"), null))
                .isInstanceOf(AccessDeniedException.class);

        when(jwtUtils.validateJwtToken("error-token")).thenThrow(new IllegalArgumentException("malformed"));
        assertThatThrownBy(() -> interceptor.preSend(connect("Bearer error-token"), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    private WebSocketAuthInterceptor interceptor() {
        return new WebSocketAuthInterceptor(jwtUtils, userDetailsService);
    }

    private Message<byte[]> connect(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorization != null) {
            accessor.addNativeHeader("Authorization", authorization);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
