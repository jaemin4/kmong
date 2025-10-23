package com.kmong.domain.outbox;


import lombok.AllArgsConstructor;
import lombok.Getter;

public class OutboxCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderOutbox {
        private String orderId;
        private String partnerApiName;
        private SendStatus partnerApiStatus;
        private String email;
        private String phoneNumber;
        private boolean isEnableEmail;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Update {
        private String orderId;
        private SendStatus kakaoStatus;
        private SendStatus smsStatus;
        private SendStatus emailStatus;
        private SendStatus naverOrderStatus;
        private Boolean callBackSuccess;
    }
}
