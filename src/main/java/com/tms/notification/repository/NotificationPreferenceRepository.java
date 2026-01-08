package com.tms.notification.repository;

import com.tms.notification.entity.Notification.NotificationChannel;
import com.tms.notification.entity.Notification.NotificationType;
import com.tms.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    List<NotificationPreference> findByUserIdAndIsEnabledTrue(String userId);

    List<NotificationPreference> findByAccountCodeAndIsEnabledTrue(String accountCode);

    @Query("SELECT p FROM NotificationPreference p WHERE p.notificationType = :type " +
           "AND p.isEnabled = true " +
           "AND (p.accountCode = :accountCode OR p.accountCode IS NULL)")
    List<NotificationPreference> findByNotificationTypeAndAccount(
        @Param("type") NotificationType notificationType,
        @Param("accountCode") String accountCode);

    List<NotificationPreference> findByNotificationTypeAndChannelAndIsEnabledTrue(
        NotificationType notificationType, NotificationChannel channel);
}
