package com.kmong.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

public class OutboxCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderOutbox {
        private String orderId;
        private String partnerApiName;
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
    }
}
