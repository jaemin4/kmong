package com.kmong.infra.order;

import com.kmong.domain.order.OrderMain;
import com.kmong.domain.order.OrderMainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderMainRepositoryImpl implements OrderMainRepository {

    private final OrderMainJpaRepository orderMainJpaRepository;

    @Override
    public OrderMain save(OrderMain orderDetail) {
        return orderMainJpaRepository.save(orderDetail);
    }

    @Override
    public boolean existsByProductOrderId(String productOrderId) {
        return orderMainJpaRepository.existsByProductOrderId(productOrderId);
    }
}
