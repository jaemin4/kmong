package com.kmong.infra.order;

import com.kmong.domain.order.OrderMain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMainJpaRepository extends JpaRepository<OrderMain, Long> {
}
