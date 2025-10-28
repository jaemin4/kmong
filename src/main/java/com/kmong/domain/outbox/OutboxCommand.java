package com.kmong.domain.outbox;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class OutboxCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static  class UpdateOfFail{
        private String orderId;
        private Boolean isFailed;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderOutbox {
        private String orderId;
        private String email;
        private String phoneNumber;
        private boolean isEnableEmail;
        private String ordererName;
        private Boolean lesim;

        // === API 상태 필드 ===
        private Boolean isCall2_1Success;
        private SendStatus isCallBack2_2Success;
        private Boolean isCall3_1Success;
        private SendStatus isCallBack3_2Success;
        private Boolean isCall2_4Success;
        private SendStatus isCallBack2_5Success;

        // === 발송 관련 ===
        private SendStatus kakaoStatus;
        private SendStatus smsStatus;
        private SendStatus emailStatus;
        private SendStatus naverOrderStatus;

    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Update {
        private String orderId;

        // 발송 상태
        private SendStatus kakaoStatus;
        private SendStatus smsStatus;
        private SendStatus emailStatus;
        private SendStatus naverOrderStatus;

        // === API 상태 업데이트 필드 ===
        private Boolean isCall2_1Success;
        private SendStatus isCallBack2_2Success;
        private Boolean isCall3_1Success;
        private SendStatus isCallBack3_2Success;
        private Boolean isCall2_4Success;
        private SendStatus isCallBack2_5Success;

        private Boolean lesim;
    }
}
