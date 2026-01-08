package com.tms.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private String notificationType;
    private String channel;
    private String recipient;
    private String subject;
    private String message;
    private String referenceType;
    private String referenceId;
    private String status;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
