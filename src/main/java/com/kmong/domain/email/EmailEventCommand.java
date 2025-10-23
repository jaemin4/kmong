package com.kmong.domain.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

public class EmailEventCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class SendEmail{
        private String orderId;
        private String email;
        private String mailSubject;
        private String mailBody;
    }
}
