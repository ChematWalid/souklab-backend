package com.project.souklab.integration.phase10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import org.springframework.amqp.core.Message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the broker contract independently of the application context.
 * Enable explicitly with PHASE10_RABBIT_INTEGRATION=true when Docker is available.
 */
@EnabledIfEnvironmentVariable(named = "PHASE10_RABBIT_INTEGRATION", matches = "true")
class Phase10RabbitMqIntegrationTest {
    private static final String USER = "phase10";
    private static final String PASSWORD = "phase10-password";
    private static final String EXCHANGE = "phase10.analytics";
    private static final String DEAD_LETTER_EXCHANGE = "phase10.analytics.dlx";
    private static final String ROUTING_KEY = "activity";
    private static final String QUEUE = "phase10.analytics.events";
    private static final String DEAD_LETTER_QUEUE = "phase10.analytics.events.dlq";

    @Test
    void durableTopologyPublisherConfirmsAndDeadLettersRejectedMessages() throws Exception {
        try (GenericContainer<?> rabbit = new GenericContainer<>("rabbitmq:3.13-management")
                .withEnv("RABBITMQ_DEFAULT_USER", USER)
                .withEnv("RABBITMQ_DEFAULT_PASS", PASSWORD)
                .withExposedPorts(5672)
                .waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1))) {
            rabbit.start();

            CachingConnectionFactory connectionFactory = new CachingConnectionFactory(
                    "127.0.0.1", rabbit.getMappedPort(5672));
            connectionFactory.setUsername(USER);
            connectionFactory.setPassword(PASSWORD);
            connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

            try {
                RabbitAdmin admin = new RabbitAdmin(connectionFactory);
                DirectExchange exchange = new DirectExchange(EXCHANGE, true, false);
                DirectExchange deadLetterExchange = new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
                Queue queue = QueueBuilder.durable(QUEUE)
                        .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                        .deadLetterRoutingKey(ROUTING_KEY)
                        .build();
                Queue deadLetterQueue = QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
                admin.declareExchange(exchange);
                admin.declareExchange(deadLetterExchange);
                admin.declareQueue(queue);
                admin.declareQueue(deadLetterQueue);
                admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY));
                admin.declareBinding(BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY));

                RabbitTemplate template = new RabbitTemplate(connectionFactory);
                template.invoke(operations -> {
                    operations.convertAndSend(EXCHANGE, ROUTING_KEY, "analytics-event");
                    operations.waitForConfirmsOrDie(5_000);
                    return null;
                });

                try (Connection connection = connectionFactory.createConnection();
                     Channel channel = connection.createChannel(false)) {
                    GetResponse response = channel.basicGet(QUEUE, false);
                    assertThat(response).isNotNull();
                    assertThat(new String(response.getBody(), StandardCharsets.UTF_8)).isEqualTo("analytics-event");
                    channel.basicReject(response.getEnvelope().getDeliveryTag(), false);
                }

                Message deadLetter = template.receive(DEAD_LETTER_QUEUE, 5_000);
                assertThat(deadLetter).isNotNull();
                assertThat(new String(deadLetter.getBody(), StandardCharsets.UTF_8)).isEqualTo("analytics-event");
            } finally {
                connectionFactory.destroy();
            }
        }
    }
}
