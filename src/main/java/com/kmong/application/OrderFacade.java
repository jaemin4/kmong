package com.kmong.application;

import com.kmong.domain.order.EsimDetail;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OrderOutbox;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OutBoxService outBoxService;

    public OrderFacadeResult.GetOrderDetail getOrderDetail(String orderId){
        List<EsimDetail> order = orderService.getOrderDetail(orderId);
        OrderOutbox outbox = outBoxService.findByOrderId(orderId);


        return OrderFacadeResult.GetOrderDetail.of(
                outbox,order
        );

    }
}
