package com.kmong.domain.access;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "esim_access_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_log_id")
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String uri;

    private String query;

    @Lob
    private String requestBody;

    @Lob
    private String responseBody;

    @Lob
    private String headers;

    private String userAgent;

    @Column(name = "remote_ip")
    private String remoteIp;

    private Integer status;

    private String threadName;

    private LocalDateTime requestAt;

    private LocalDateTime responseAt;

    private long durationMs;

    public static AccessLog of(com.kmong.infra.access.AccessLogConsumerCommand.Save save) {
        String contentType = extractContentType(save.getHeaders());
        return AccessLog.builder()
                .userId(save.getUserId())
                .role(save.getRole())
                .method(save.getMethod())
                .uri(save.getUri())
                .query(save.getQuery())
                .requestBody(filterBodyByContentType(contentType, save.getRequestBody()))
                .responseBody(filterBodyByContentType(contentType, save.getResponseBody()))
                .headers(save.getHeaders())
                .userAgent(save.getUserAgent())
                .remoteIp(save.getRemoteIp())
                .status(save.getStatus())
                .threadName(save.getThreadName())
                .requestAt(save.getRequestAt())
                .responseAt(save.getResponseAt())
                .durationMs(save.getDurationMs())
                .build();
    }

    private static String extractContentType(String headersJson) {
        if (headersJson == null) return null;
        try {
            String[] headers = headersJson.split(",");
            for (String header : headers) {
                String[] parts = header.split("=", 2);
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase("content-type")) {
                    return parts[1].trim().toLowerCase();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String filterBodyByContentType(String contentType, String body) {
        if (contentType == null) return body;
        if (isTextContent(contentType)) {
            return body;
        } else {
            return "[BINARY_CONTENT_OMITTED]";
        }
    }

    private static boolean isTextContent(String contentType) {
        return contentType.startsWith("text/")
                || contentType.contains("json")
                || contentType.contains("xml")
                || contentType.contains("html")
                || contentType.contains("x-www-form-urlencoded");
    }
}
