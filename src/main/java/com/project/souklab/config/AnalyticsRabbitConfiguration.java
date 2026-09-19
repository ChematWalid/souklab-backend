package com.project.souklab.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.analytics.rabbit.enabled", havingValue = "true")
public class AnalyticsRabbitConfiguration {
    @Bean
    ConnectionFactory analyticsRabbitConnectionFactory(AnalyticsRabbitProperties p) {
        CachingConnectionFactory factory = new CachingConnectionFactory(p.getHost(), p.getPort());
        factory.setUsername(p.getUsername());
        factory.setPassword(p.getPassword());
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        return factory;
    }

    @Bean
    RabbitTemplate analyticsRabbitTemplate(ConnectionFactory factory) { return new RabbitTemplate(factory); }

    @Bean(name = "rabbitListenerContainerFactory")
    SimpleRabbitListenerContainerFactory analyticsRabbitListenerContainerFactory(
            ConnectionFactory factory, AnalyticsRabbitProperties properties) {
        SimpleRabbitListenerContainerFactory container = new SimpleRabbitListenerContainerFactory();
        container.setConnectionFactory(factory);
        container.setDefaultRequeueRejected(false);
        container.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxRetries(Math.max(0, properties.getMaxAttempts() - 1))
                .backOffOptions(properties.getRetryBackoff().toMillis(), properties.getRetryMultiplier(),
                        properties.getMaxRetryBackoff().toMillis())
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return container;
    }

    @Bean
    DirectExchange analyticsExchange(AnalyticsRabbitProperties p) { return new DirectExchange(p.getExchange(), true, false); }

    @Bean
    DirectExchange analyticsDeadLetterExchange(AnalyticsRabbitProperties p) { return new DirectExchange(p.getDeadLetterExchange(), true, false); }

    @Bean
    Queue analyticsQueue(AnalyticsRabbitProperties p) {
        return QueueBuilder.durable(p.getQueue()).deadLetterExchange(p.getDeadLetterExchange()).deadLetterRoutingKey(p.getRoutingKey()).build();
    }

    @Bean
    Queue analyticsDeadLetterQueue(AnalyticsRabbitProperties p) { return QueueBuilder.durable(p.getDeadLetterQueue()).build(); }

    @Bean
    Binding analyticsBinding(Queue analyticsQueue, DirectExchange analyticsExchange, AnalyticsRabbitProperties p) {
        return BindingBuilder.bind(analyticsQueue).to(analyticsExchange).with(p.getRoutingKey());
    }

    @Bean
    Binding analyticsDeadLetterBinding(Queue analyticsDeadLetterQueue, DirectExchange analyticsDeadLetterExchange, AnalyticsRabbitProperties p) {
        return BindingBuilder.bind(analyticsDeadLetterQueue).to(analyticsDeadLetterExchange).with(p.getRoutingKey());
    }
}
