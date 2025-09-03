package com.kmong.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class NotificationCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Register{
        private String kakaoAccessToken;
        private String subject;
        private String content;
        private String keyString;
        private String keyStringRange;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Update{
        private Long notificationId;
        private String kakaoAccessToken;
        private String subject;
        private String content;
        private String keyString;
        private String keyStringRange;
    }

}
