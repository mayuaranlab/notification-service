package com.tms.notification.service;

import com.tms.notification.entity.Notification;
import com.tms.notification.entity.Notification.NotificationChannel;
import com.tms.notification.entity.Notification.NotificationStatus;
import com.tms.notification.entity.Notification.NotificationType;
import com.tms.notification.entity.NotificationPreference;
import com.tms.notification.repository.NotificationPreferenceRepository;
import com.tms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;

    @Transactional
    public void sendNotification(NotificationType type, String accountCode,
                                  String subject, String message,
                                  String referenceType, String referenceId,
                                  String correlationId) {

        log.info("Processing notification: type={}, accountCode={}, referenceId={}",
            type, accountCode, referenceId);

        // Find preferences for this notification type and account
        List<NotificationPreference> preferences = preferenceRepository
            .findByNotificationTypeAndAccount(type, accountCode);

        if (preferences.isEmpty()) {
            log.debug("No notification preferences found for type={}, accountCode={}", type, accountCode);
            return;
        }

        for (NotificationPreference preference : preferences) {
            Notification notification = Notification.builder()
                .notificationType(type)
                .channel(preference.getChannel())
                .recipient(preference.getRecipient())
                .subject(subject)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .correlationId(correlationId)
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();

            notification = notificationRepository.save(notification);
            processNotification(notification);
        }
    }

    @Async
    public void processNotification(Notification notification) {
        try {
            log.info("Sending notification: notificationId={}, channel={}, recipient={}",
                notification.getNotificationId(), notification.getChannel(), notification.getRecipient());

            boolean success = switch (notification.getChannel()) {
                case EMAIL -> sendEmail(notification);
                case SLACK -> sendSlack(notification);
                case WEBHOOK -> sendWebhook(notification);
                default -> {
                    log.warn("Unsupported channel: {}", notification.getChannel());
                    yield false;
                }
            };

            if (success) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                log.info("Notification sent successfully: notificationId={}", notification.getNotificationId());
            } else {
                handleFailure(notification, "Send operation returned false");
            }

        } catch (Exception e) {
            log.error("Failed to send notification: notificationId={}",
                notification.getNotificationId(), e);
            handleFailure(notification, e.getMessage());
        }

        notificationRepository.save(notification);
    }

    private boolean sendEmail(Notification notification) {
        return emailService.sendEmail(
            notification.getRecipient(),
            notification.getSubject(),
            notification.getMessage()
        );
    }

    private boolean sendSlack(Notification notification) {
        // Placeholder for Slack integration
        log.info("Slack notification (simulated): recipient={}, message={}",
            notification.getRecipient(), notification.getMessage());
        return true;
    }

    private boolean sendWebhook(Notification notification) {
        // Placeholder for Webhook integration
        log.info("Webhook notification (simulated): recipient={}, message={}",
            notification.getRecipient(), notification.getMessage());
        return true;
    }

    private void handleFailure(Notification notification, String errorMessage) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setErrorMessage(errorMessage);

        if (notification.getRetryCount() >= 3) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("Notification failed after {} retries: notificationId={}",
                notification.getRetryCount(), notification.getNotificationId());
        } else {
            notification.setStatus(NotificationStatus.RETRY);
            log.warn("Notification will be retried: notificationId={}, attempt={}",
                notification.getNotificationId(), notification.getRetryCount());
        }
    }

    // Event handlers for different notification types

    public void handleTradeBooked(Map<String, Object> tradeEvent) {
        String tradeId = (String) tradeEvent.get("tradeId");
        String accountCode = (String) tradeEvent.get("accountCode");
        String symbol = (String) tradeEvent.get("symbol");
        String side = (String) tradeEvent.get("side");
        String quantity = (String) tradeEvent.get("quantity");
        String correlationId = (String) tradeEvent.get("correlationId");

        String subject = String.format("Trade Booked: %s %s %s", side, quantity, symbol);
        String message = String.format(
            "Trade has been booked successfully.\n\n" +
            "Trade ID: %s\nAccount: %s\nSymbol: %s\nSide: %s\nQuantity: %s",
            tradeId, accountCode, symbol, side, quantity);

        sendNotification(NotificationType.TRADE_BOOKED, accountCode, subject, message,
            "TRADE", tradeId, correlationId);
    }

    public void handleRiskAlert(Map<String, Object> alertEvent) {
        String alertId = (String) alertEvent.get("alertId");
        String accountCode = (String) alertEvent.get("accountCode");
        String severity = (String) alertEvent.get("severity");
        String alertMessage = (String) alertEvent.get("message");
        String correlationId = (String) alertEvent.get("correlationId");

        String subject = String.format("[%s] Risk Alert: %s", severity, accountCode);
        String message = String.format(
            "Risk Alert Generated\n\n" +
            "Alert ID: %s\nAccount: %s\nSeverity: %s\n\nDetails:\n%s",
            alertId, accountCode, severity, alertMessage);

        sendNotification(NotificationType.RISK_ALERT, accountCode, subject, message,
            "RISK_ALERT", alertId, correlationId);
    }

    public void handleSettlementEvent(Map<String, Object> settlementEvent, NotificationType type) {
        String settlementId = (String) settlementEvent.get("settlementId");
        String accountCode = (String) settlementEvent.get("accountCode");
        String status = (String) settlementEvent.get("status");
        String correlationId = (String) settlementEvent.get("correlationId");

        String subject = String.format("Settlement %s: %s", status, settlementId);
        String message = String.format(
            "Settlement Status Update\n\n" +
            "Settlement ID: %s\nAccount: %s\nStatus: %s",
            settlementId, accountCode, status);

        sendNotification(type, accountCode, subject, message,
            "SETTLEMENT", settlementId, correlationId);
    }

    public List<Notification> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipient(recipient);
    }

    public List<Notification> getFailedNotifications() {
        return notificationRepository.findByStatus(NotificationStatus.FAILED);
    }
}
