package com.kmong.domain.outbox;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class OutboxCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderOutbox {
        private String orderProductId;
        private String partnerApiName;
        private SendStatus partnerApiStatus;
    }
}
