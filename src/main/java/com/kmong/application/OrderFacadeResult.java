package com.kmong.application;

import com.kmong.domain.order.EsimDetail;
import com.kmong.domain.outbox.OrderOutbox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

public class OrderFacadeResult {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class GetOrderDetail{
        private OrderOutbox orderOutbox;
        private List<EsimDetail> esimDetailList;
    }
}
