package com.kmong.infra.outbox;

import com.kmong.domain.outbox.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutbox, Long> {
}
