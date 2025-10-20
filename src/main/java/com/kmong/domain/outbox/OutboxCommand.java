package com.kmong.domain.outbox;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

public class OutboxCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderOutbox {
        private String productOrderId;
        private String partnerApiName;
        private SendStatus partnerApiStatus;

        private Boolean enableEmail;
        private String email;
        private String phoneNumber;
        private Map<String, Object> row;

    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Update {
        private String productOrderId;
        private SendStatus kakaoStatus;
        private SendStatus emailStatus;
        private SendStatus naverOrderStatus;
        private SendStatus smsStatus;
    }
}
