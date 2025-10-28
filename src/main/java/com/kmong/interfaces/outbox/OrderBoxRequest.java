package com.kmong.interfaces.outbox;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class OrderBoxRequest {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class ResendEmail {

    }
}
