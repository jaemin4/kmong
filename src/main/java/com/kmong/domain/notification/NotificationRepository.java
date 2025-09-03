package com.kmong.domain.notification;

import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    Long count();

}
