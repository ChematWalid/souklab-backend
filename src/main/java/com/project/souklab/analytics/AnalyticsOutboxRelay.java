package com.project.souklab.analytics;

import com.project.souklab.config.AnalyticsRabbitProperties;
import com.project.souklab.dao.analytics.AnalyticsOutboxRepository;
import com.project.souklab.model.analytics.AnalyticsOutboxEvent;
import com.project.souklab.model.analytics.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Duration;
import org.springframework.data.domain.PageRequest;

@Component
@ConditionalOnProperty(name = "app.analytics.rabbit.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsOutboxRelay {
    private final AnalyticsOutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final AnalyticsRabbitProperties properties;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${app.analytics.rabbit.poll-interval}")
    public void relayPendingEvents() {
        LocalDateTime now = LocalDateTime.now(clock);
        repository.findReadyByStatus(
                        OutboxStatus.PENDING, now,
                        PageRequest.of(0, properties.getRelayBatchSize()))
                .forEach(this::publishOne);
    }

    public void publishOne(AnalyticsOutboxEvent event) {
        transactionTemplate.executeWithoutResult(status -> publishOneInTransaction(event));
    }

    private void publishOneInTransaction(AnalyticsOutboxEvent event) {
        try {
            rabbitTemplate.invoke(operations -> {
                operations.convertAndSend(properties.getExchange(), properties.getRoutingKey(), event.getPayloadJson());
                operations.waitForConfirmsOrDie(properties.getConfirmTimeout().toMillis());
                return null;
            });
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now(clock));
            event.setNextAttemptAt(null);
            repository.save(event);
        } catch (Exception failure) {
            event.setAttemptCount(event.getAttemptCount() + 1);
            event.setLastError(failure.getMessage());
            if (event.getAttemptCount() >= properties.getMaxAttempts()) {
                if (publishToDeadLetter(event)) {
                    event.setStatus(OutboxStatus.DEAD_LETTER);
                    event.setNextAttemptAt(null);
                } else {
                    event.setNextAttemptAt(nextAttempt(event.getAttemptCount()));
                }
            } else {
                event.setNextAttemptAt(nextAttempt(event.getAttemptCount()));
            }
            repository.save(event);
            log.warn("Analytics outbox delivery failed for {} (attempt {})", event.getId(), event.getAttemptCount());
        }
    }

    private boolean publishToDeadLetter(AnalyticsOutboxEvent event) {
        try {
            rabbitTemplate.invoke(operations -> {
                operations.convertAndSend(properties.getDeadLetterExchange(), properties.getRoutingKey(), event.getPayloadJson());
                operations.waitForConfirmsOrDie(properties.getConfirmTimeout().toMillis());
                return null;
            });
            return true;
        } catch (Exception deadLetterFailure) {
            log.warn("Could not publish analytics outbox event {} to the dead-letter exchange", event.getId(), deadLetterFailure);
            return false;
        }
    }

    private LocalDateTime nextAttempt(int attempt) {
        long baseMillis = properties.getRetryBackoff().toMillis();
        double multiplier = Math.pow(properties.getRetryMultiplier(), Math.max(0, attempt - 1));
        long delayMillis = Math.min(properties.getMaxRetryBackoff().toMillis(),
                (long) Math.ceil(baseMillis * multiplier));
        return LocalDateTime.now(clock).plus(Duration.ofMillis(delayMillis));
    }
}
