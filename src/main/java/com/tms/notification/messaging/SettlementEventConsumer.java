package com.tms.notification.messaging;

import com.tms.common.config.kafka.KafkaTopics;
import com.tms.common.observability.logging.CorrelationIdFilter;
import com.tms.notification.entity.Notification.NotificationType;
import com.tms.notification.service.NotificationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = KafkaTopics.SETTLEMENTS,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @CircuitBreaker(name = "kafka-consumer", fallbackMethod = "handleSettlementEventFallback")
    public void handleSettlementEvent(ConsumerRecord<String, Map<String, Object>> record,
                                       Acknowledgment acknowledgment) {
        Map<String, Object> settlementEvent = record.value();
        String settlementId = (String) settlementEvent.get("settlementId");
        String eventType = (String) settlementEvent.get("eventType");
        String correlationId = (String) settlementEvent.get("correlationId");

        try {
            MDC.put(CorrelationIdFilter.CORRELATION_ID_KEY, correlationId);
            log.info("Received settlement event for notification: settlementId={}, eventType={}",
                settlementId, eventType);

            NotificationType notificationType = switch (eventType) {
                case "SettlementCreated" -> NotificationType.SETTLEMENT_CREATED;
                case "SettlementConfirmed" -> NotificationType.SETTLEMENT_CONFIRMED;
                case "SettlementFailed" -> NotificationType.SETTLEMENT_FAILED;
                default -> null;
            };

            if (notificationType != null) {
                notificationService.handleSettlementEvent(settlementEvent, notificationType);
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process settlement notification: settlementId={}", settlementId, e);
            throw e;
        } finally {
            MDC.remove(CorrelationIdFilter.CORRELATION_ID_KEY);
        }
    }

    public void handleSettlementEventFallback(ConsumerRecord<String, Map<String, Object>> record,
                                               Acknowledgment acknowledgment,
                                               Exception e) {
        String settlementId = (String) record.value().get("settlementId");
        log.error("Circuit breaker open, skipping notification for settlement: settlementId={}",
            settlementId, e);
        acknowledgment.acknowledge();
    }
}
