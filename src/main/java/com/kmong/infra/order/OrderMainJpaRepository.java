package com.kmong.infra.order;

import com.kmong.domain.order.OrderMain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderMainJpaRepository extends JpaRepository<OrderMain, Long> {
    boolean existsByProductOrderId(String productOrderId);

    Optional<OrderMain> findByOrderId(String orderId);
}
