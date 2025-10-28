package com.kmong.infra.outbox;

import com.kmong.domain.outbox.OrderOutbox;
import com.kmong.domain.outbox.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderOutBoxRepositoryImpl implements OrderOutboxRepository {

    private final OrderOutboxJpaRepository orderOutboxJpaRepository;

    @Override
    public OrderOutbox save(OrderOutbox orderOutbox) {
        return orderOutboxJpaRepository.save(orderOutbox);
    }

    @Override
    public Optional<OrderOutbox> findByOrderId(String orderId) {
        return orderOutboxJpaRepository.findByOrderId(orderId);
    }

    @Override
    public void deleteByOrderId(String orderId) {
        orderOutboxJpaRepository.deleteByOrderId(orderId);
    }
}
