package com.kmong.infra.order;

import com.kmong.domain.order.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderDetailJpaRepository extends JpaRepository<OrderDetail, Long> {
    Optional<OrderDetail> findByOrderId(String orderId);
}
