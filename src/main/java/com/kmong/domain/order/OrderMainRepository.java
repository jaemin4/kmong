package com.kmong.domain.order;

public interface OrderMainRepository {

    OrderMain save(OrderMain orderDetail);

    boolean existsByProductOrderId(String productOrderId);
}
