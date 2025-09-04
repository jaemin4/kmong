package com.kmong.domain.notification;

import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    Long count();
    Optional<Notification> findByeKey(Integer eKey);
}
