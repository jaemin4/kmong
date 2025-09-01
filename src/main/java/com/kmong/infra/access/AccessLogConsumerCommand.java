package com.kmong.infra.access;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessLogConsumerCommand {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class Save {
        private Long userId;
        private String role;
        private String method;
        private String uri;
        private String query;
        private String requestBody;
        private String responseBody;
        private String headers;
        private String userAgent;
        private String remoteIp;
        private Integer status;
        private String threadName;
        private LocalDateTime requestAt;
        private LocalDateTime responseAt;
        private long durationMs;


    }
}
