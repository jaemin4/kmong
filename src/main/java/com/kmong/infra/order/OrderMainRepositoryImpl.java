package com.kmong.infra.order;

import com.kmong.domain.order.OrderMain;
import com.kmong.domain.order.OrderMainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderMainRepositoryImpl implements OrderMainRepository {

    private final OrderMainJpaRepository orderMainJpaRepository;
    private final OrderMainMybatisRepository orderMainMybatisRepository;

    @Override
    public OrderMain save(OrderMain orderDetail) {
        return orderMainJpaRepository.save(orderDetail);
    }

    @Override
    public boolean existsByProductOrderId(String productOrderId) {
        return orderMainJpaRepository.existsByProductOrderId(productOrderId);
    }

    @Override
    public List<OrderMain> findAllByKeyword(String keyword) {
        return orderMainMybatisRepository.findAllByKeyword(keyword);
    }
}
