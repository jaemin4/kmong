package com.kmong.infra.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class EmailConsumerCommand {

    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    @Getter
    public static class Issue{
        private String orderId;
        private String email;
        private String subject;
        private String body;
        private String from;
    }

}
