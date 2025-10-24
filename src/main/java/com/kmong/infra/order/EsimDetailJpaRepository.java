package com.kmong.infra.order;

import com.kmong.domain.order.EsimDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EsimDetailJpaRepository extends JpaRepository<EsimDetail, Long> {
    List<EsimDetail> findAllByOrderId(String orderId);
}
