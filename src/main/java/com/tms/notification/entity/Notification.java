package com.tms.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "recipient", nullable = false, length = 200)
    private String recipient;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        TRADE_BOOKED,
        TRADE_REJECTED,
        POSITION_UPDATE,
        RISK_ALERT,
        SETTLEMENT_CREATED,
        SETTLEMENT_CONFIRMED,
        SETTLEMENT_FAILED,
        DAILY_SUMMARY
    }

    public enum NotificationChannel {
        EMAIL,
        SMS,
        WEBHOOK,
        SLACK,
        IN_APP
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        RETRY
    }
}
