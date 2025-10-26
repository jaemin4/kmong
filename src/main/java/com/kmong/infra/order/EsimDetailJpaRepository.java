package com.kmong.infra.order;

import com.kmong.domain.order.EsimDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EsimDetailJpaRepository extends JpaRepository<EsimDetail, Long> {
    List<EsimDetail> findAllByOrderId(String orderId);

    Optional<EsimDetail> findByRcode(String rcode);
}
