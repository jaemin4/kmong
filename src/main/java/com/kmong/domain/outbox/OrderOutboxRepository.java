package com.kmong.domain.outbox;

import java.util.Optional;

public interface OrderOutboxRepository {

    OrderOutbox save(OrderOutbox orderOutbox);

    Optional<OrderOutbox> findByOrderId(String orderId);
}
