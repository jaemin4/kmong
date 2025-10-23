package com.kmong.infra.order;

import com.kmong.domain.order.OrderMain;
import com.kmong.domain.order.OrderMainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@RequiredArgsConstructor
public class OrderMainRepositoryImpl implements OrderMainRepository {

    private final OrderMainJpaRepository orderMainJpaRepository;
    private final OrderMainMybatisRepository orderMainMybatisRepository;

    @PersistenceContext
    private EntityManager em;


    @Override
    public OrderMain save(OrderMain orderDetail) {
        return orderMainJpaRepository.save(orderDetail);
    }

    @Override
    public boolean existsByProductOrderId(String productOrderId) {
        return orderMainJpaRepository.existsByProductOrderId(productOrderId);
    }

    @Override
    public List<OrderMain> findAllByKeyword(String keyword, LocalDate start, LocalDate end) {
        return orderMainMybatisRepository.findAllByKeyword(keyword, start, end);
    }


    public boolean existsMainByOrderId(String orderId) {
        String jpql = "SELECT COUNT(o) > 0 FROM OrderMain o WHERE o.orderNumber = :orderId";
        return em.createQuery(jpql, Boolean.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
    }

}
