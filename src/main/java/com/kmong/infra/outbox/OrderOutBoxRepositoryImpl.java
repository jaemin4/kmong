package com.kmong.infra.outbox;

import com.kmong.domain.outbox.OrderOutbox;
import com.kmong.domain.outbox.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderOutBoxRepositoryImpl implements OrderOutboxRepository {

    private final OrderOutboxJpaRepository orderOutboxJpaRepository;

    @Override
    public OrderOutbox save(OrderOutbox orderOutbox) {
        return orderOutboxJpaRepository.save(orderOutbox);
    }
}
