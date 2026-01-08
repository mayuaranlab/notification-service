package com.tms.notification.controller;

import com.tms.notification.dto.NotificationPreferenceRequest;
import com.tms.notification.dto.NotificationResponse;
import com.tms.notification.entity.Notification;
import com.tms.notification.entity.NotificationPreference;
import com.tms.notification.repository.NotificationPreferenceRepository;
import com.tms.notification.repository.NotificationRepository;
import com.tms.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @GetMapping
    @Operation(summary = "Get notifications for a recipient")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam String recipient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Notification> notifications = notificationRepository
            .findByRecipientOrderByCreatedAtDesc(recipient, PageRequest.of(page, size));

        return ResponseEntity.ok(notifications.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long notificationId) {
        return notificationRepository.findById(notificationId)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/failed")
    @Operation(summary = "Get failed notifications")
    public ResponseEntity<List<NotificationResponse>> getFailedNotifications() {
        List<Notification> failed = notificationService.getFailedNotifications();
        return ResponseEntity.ok(failed.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @PostMapping("/{notificationId}/retry")
    @Operation(summary = "Retry a failed notification")
    public ResponseEntity<NotificationResponse> retryNotification(@PathVariable Long notificationId) {
        return notificationRepository.findById(notificationId)
            .map(notification -> {
                notification.setRetryCount(0);
                notification.setStatus(Notification.NotificationStatus.PENDING);
                notificationRepository.save(notification);
                notificationService.processNotification(notification);
                return ResponseEntity.ok(toResponse(notification));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // Preferences endpoints

    @GetMapping("/preferences/{userId}")
    @Operation(summary = "Get notification preferences for a user")
    public ResponseEntity<List<NotificationPreference>> getPreferences(@PathVariable String userId) {
        List<NotificationPreference> preferences = preferenceRepository.findByUserIdAndIsEnabledTrue(userId);
        return ResponseEntity.ok(preferences);
    }

    @PostMapping("/preferences")
    @Operation(summary = "Create notification preference")
    public ResponseEntity<NotificationPreference> createPreference(
            @Valid @RequestBody NotificationPreferenceRequest request) {

        NotificationPreference preference = NotificationPreference.builder()
            .userId(request.getUserId())
            .accountCode(request.getAccountCode())
            .notificationType(request.getNotificationType())
            .channel(request.getChannel())
            .recipient(request.getRecipient())
            .isEnabled(true)
            .build();

        preference = preferenceRepository.save(preference);
        return ResponseEntity.ok(preference);
    }

    @DeleteMapping("/preferences/{preferenceId}")
    @Operation(summary = "Disable notification preference")
    public ResponseEntity<Void> disablePreference(@PathVariable Long preferenceId) {
        return preferenceRepository.findById(preferenceId)
            .map(preference -> {
                preference.setIsEnabled(false);
                preferenceRepository.save(preference);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
            .notificationId(notification.getNotificationId())
            .notificationType(notification.getNotificationType().name())
            .channel(notification.getChannel().name())
            .recipient(notification.getRecipient())
            .subject(notification.getSubject())
            .message(notification.getMessage())
            .referenceType(notification.getReferenceType())
            .referenceId(notification.getReferenceId())
            .status(notification.getStatus().name())
            .retryCount(notification.getRetryCount())
            .errorMessage(notification.getErrorMessage())
            .sentAt(notification.getSentAt())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
