package com.tms.notification.dto;

import com.tms.notification.entity.Notification.NotificationChannel;
import com.tms.notification.entity.Notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    private String accountCode;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotBlank(message = "Recipient is required")
    private String recipient;
}
