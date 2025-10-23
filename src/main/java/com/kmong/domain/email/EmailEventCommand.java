package com.kmong.domain.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

public class EmailEventCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class SendEmail{
        private String productOrderId;
        private String email;
        private String mailSubject;
        private String mailBody;
        private boolean emailEnabled;
        private boolean isApiRequest;
        private String phoneNumber;
        private String partnerApiName;

    }
}
