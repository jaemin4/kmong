package com.kmong.interfaces.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class NotificationRequest {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Register{
        private String kakaoAccessToken;
        private String subject;
        private String content;
        private String keyString;
        private String keyStringRange;
    }

}
