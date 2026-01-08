package com.tms.notification.repository;

import com.tms.notification.entity.Notification;
import com.tms.notification.entity.Notification.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByRecipient(String recipient);

    List<Notification> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);

    Page<Notification> findByRecipientOrderByCreatedAtDesc(String recipient, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' " +
           "OR (n.status = 'RETRY' AND n.retryCount < 3)")
    List<Notification> findNotificationsToProcess();

    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' " +
           "AND n.createdAt >= :since")
    List<Notification> findFailedNotificationsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status " +
           "AND n.createdAt >= :since")
    long countByStatusSince(@Param("status") NotificationStatus status,
                            @Param("since") LocalDateTime since);

    List<Notification> findByCorrelationId(String correlationId);
}
