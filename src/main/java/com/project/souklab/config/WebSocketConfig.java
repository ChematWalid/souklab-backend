package com.project.souklab.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final AppProperties appProperties;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(appProperties.getChat().getWebsocketEndpoint())
                .setAllowedOriginPatterns(appProperties.getCors().getAllowedOrigins().toArray(String[]::new))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        AppProperties.Relay relay = appProperties.getRelay();
        registry.enableStompBrokerRelay(appProperties.getChat().getBrokerDestinationPrefixes().split(","))
                .setRelayHost(relay.getHost())
                .setRelayPort(relay.getPort())
                .setClientLogin(relay.getClientLogin())
                .setClientPasscode(relay.getClientPasscode())
                .setSystemLogin(relay.getSystemLogin())
                .setSystemPasscode(relay.getSystemPasscode())
                .setUserDestinationBroadcast(appProperties.getChat().getUnresolvedUserDestination())
                .setUserRegistryBroadcast(appProperties.getChat().getUserRegistryBroadcast());

        registry.setApplicationDestinationPrefixes(appProperties.getChat().getApplicationDestinationPrefix());
        registry.setUserDestinationPrefix(appProperties.getChat().getUserDestinationPrefix());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
