package com.kmong.infra.order;

import com.kmong.domain.order.OrderDetail;
import com.kmong.domain.order.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderDetailRepositoryImpl implements OrderDetailRepository {

    private final OrderDetailJpaRepository orderDetailJpaRepository;

    @Override
    public OrderDetail save(OrderDetail orderDetail) {
        return orderDetailJpaRepository.save(orderDetail);
    }

}
