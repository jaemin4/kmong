package com.kmong.domain.order;

import java.util.List;

public interface OrderMainRepository {

    OrderMain save(OrderMain orderDetail);

    boolean existsByProductOrderId(String productOrderId);

    List<OrderMain> findAllByKeyword(String keyword);

}
