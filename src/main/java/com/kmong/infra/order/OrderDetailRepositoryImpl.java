package com.kmong.infra.order;

import com.kmong.domain.order.OrderDetail;
import com.kmong.domain.order.OrderDetailRepository;
import com.kmong.domain.order.OrderMain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderDetailRepositoryImpl implements OrderDetailRepository {

    private final OrderDetailJpaRepository orderDetailJpaRepository;

    @Override
    public OrderDetail save(OrderDetail orderDetail) {
        return orderDetailJpaRepository.save(orderDetail);
    }

    @Override
    public Optional<OrderDetail> findByOrderId(String orderId) {
        return orderDetailJpaRepository.findByOrderId(orderId);
    }
}
