package com.kmong.domain.outbox;

public interface OrderOutboxRepository {

    OrderOutbox save(OrderOutbox orderOutbox);

}
