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
public class RiskAlertConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = KafkaTopics.RISK_ALERTS,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @CircuitBreaker(name = "kafka-consumer", fallbackMethod = "handleRiskAlertFallback")
    public void handleRiskAlert(ConsumerRecord<String, Map<String, Object>> record,
                                 Acknowledgment acknowledgment) {
        Map<String, Object> alertEvent = record.value();
        String alertId = (String) alertEvent.get("alertId");
        String correlationId = (String) alertEvent.get("correlationId");

        try {
            MDC.put(CorrelationIdFilter.CORRELATION_ID_KEY, correlationId);
            log.info("Received risk alert for notification: alertId={}", alertId);

            notificationService.handleRiskAlert(alertEvent);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process risk alert notification: alertId={}", alertId, e);
            throw e;
        } finally {
            MDC.remove(CorrelationIdFilter.CORRELATION_ID_KEY);
        }
    }

    public void handleRiskAlertFallback(ConsumerRecord<String, Map<String, Object>> record,
                                         Acknowledgment acknowledgment,
                                         Exception e) {
        String alertId = (String) record.value().get("alertId");
        log.error("Circuit breaker open, skipping notification for alert: alertId={}", alertId, e);
        acknowledgment.acknowledge();
    }
}
