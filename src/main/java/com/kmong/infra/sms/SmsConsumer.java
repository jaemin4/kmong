package com.kmong.infra.sms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SmsConsumer {

    @Getter
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class Issue {
        private String phoneNumber;
        private String body;
    }
}
