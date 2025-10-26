package com.kmong.domain.sms;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SmsEventCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Issue{
        private String phoneNumber;
        private String smsBody;
        private String orderId;

    }
}
