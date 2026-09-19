package com.project.souklab.integration.phase10;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.analytics.AnalyticsEvent;
import com.project.souklab.analytics.AnalyticsMetric;
import com.project.souklab.config.AnalyticsRabbitProperties;
import com.project.souklab.dao.analytics.AnalyticsProcessedEventRepository;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the application-managed analytics RabbitMQ connection, durable
 * topology, and publisher-confirm path against the configured broker.
 * Enable explicitly with ANALYTICS_RABBIT_ENABLED=true.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANALYTICS_RABBIT_ENABLED", matches = "true")
class Phase10AnalyticsRabbitApplicationIntegrationTest {

    @Autowired
    private AnalyticsRabbitProperties properties;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalyticsProcessedEventRepository processedEvents;

    @Test
    void applicationTopologyIsReachableAndPublisherConfirms() throws Exception {
        String eventId = UUID.randomUUID().toString();
        String body = objectMapper.writeValueAsString(Map.of(
                AnalyticsMetric.Payload.Event.ID.value(), eventId,
                AnalyticsMetric.Payload.Event.TYPE.value(), AnalyticsEvent.Authentication.Login.SUCCEEDED.value(),
                AnalyticsMetric.Payload.Event.TIME.value(), LocalDateTime.now().toString(),
                AnalyticsMetric.Payload.Actor.ID.value(), "",
                AnalyticsMetric.Payload.Subject.ID.value(), "",
                AnalyticsMetric.Payload.Metadata.VALUE.value(), Map.of()));
        try (Connection connection = connectionFactory.createConnection();
             Channel channel = connection.createChannel(false)) {
            channel.exchangeDeclarePassive(properties.getExchange());
            assertThat(channel.queueDeclarePassive(properties.getQueue()).getMessageCount()).isGreaterThanOrEqualTo(0);

            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            template.invoke(operations -> {
                operations.convertAndSend(properties.getExchange(), properties.getRoutingKey(), body);
                operations.waitForConfirmsOrDie(properties.getConfirmTimeout().toMillis());
                return null;
            });

            for (int attempt = 0; attempt < 100 && !processedEvents.existsByEventId(eventId); attempt++) {
                Thread.sleep(100);
            }
            assertThat(processedEvents.existsByEventId(eventId)).isTrue();
            channel.queuePurge(properties.getQueue());
        }
    }
}
