package com.kmong.infra.order;

import com.kmong.domain.order.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderDetail, Long> {
}
