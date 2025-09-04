package com.kmong.infra.outbox;

import com.kmong.domain.outbox.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutbox, Long> {
    Optional<OrderOutbox> findByProductOrderId(String productOrderId);
}
