package com.kmong.infra.notification;

import com.kmong.domain.notification.Notification;
import com.kmong.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationJpaRepository.findById(id);
    }

    @Override
    public Long count() {
        return notificationJpaRepository.count();
    }

    @Override
    public Optional<Notification> findByeKey(Integer eKey) {
        return notificationJpaRepository.findByeKey(eKey);
    }
}
