package com.kmong.infra.order;

import com.kmong.domain.order.EsimDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsimDetailJpaRepository extends JpaRepository<EsimDetail, Long> {
}
