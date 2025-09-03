package com.kmong.infra.notification;

import com.kmong.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
}
