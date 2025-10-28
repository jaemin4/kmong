package com.kmong.support.valid;

import com.kmong.domain.outbox.OrderOutbox;

public class OrderOutBoxFacadeValid {

    public static void resendEmailValid(OrderOutbox orderOutbox) {
        if(orderOutbox == null) {
            throw new RuntimeException("orderOutbox is null");
        }

    }

    public static void resendSmsValid(OrderOutbox orderOutbox) {
        if(orderOutbox == null) {
            throw new RuntimeException("orderOutbox is null");
        }

    }
}
