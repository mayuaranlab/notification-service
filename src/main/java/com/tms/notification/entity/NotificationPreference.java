package com.tms.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preference")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "account_code", length = 50)
    private String accountCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private Notification.NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private Notification.NotificationChannel channel;

    @Column(name = "recipient", nullable = false, length = 200)
    private String recipient;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
