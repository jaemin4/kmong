package com.kmong.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class NotificationResult {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Get{
        private Notification notification;
    }
}
