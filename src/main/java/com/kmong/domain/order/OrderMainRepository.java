package com.kmong.domain.order;

import java.time.LocalDate;
import java.util.List;

public interface OrderMainRepository {

    OrderMain save(OrderMain orderDetail);

    boolean existsByProductOrderId(String productOrderId);

    List<OrderMain> findAllByKeyword(String keyword, LocalDate start, LocalDate end);

    boolean existsMainByOrderId(String orderId);

}
