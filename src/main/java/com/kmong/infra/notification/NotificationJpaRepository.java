package com.kmong.infra.notification;

import com.kmong.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByeKey(Integer eKey);
}
