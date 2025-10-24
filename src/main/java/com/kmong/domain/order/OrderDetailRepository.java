package com.kmong.domain.order;

import io.jsonwebtoken.security.Jwks;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository {

    OrderDetail save(OrderDetail orderDetail);

    Optional<OrderDetail> findByOrderId(String orderId);
}
