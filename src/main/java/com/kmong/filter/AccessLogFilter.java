package com.kmong.filter;

import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.support.response.APIResponse;
import com.kmong.support.utils.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class AccessLogFilter extends OncePerRequestFilter {

    private final RabbitTemplate rabbitTemplate;

    private static final Set<String> EXCLUDE_PREFIXES = Set.of(
            "/healthz", "/actuator", "/swagger", "/v3/api-docs"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String uri = request.getRequestURI();
        return EXCLUDE_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        LocalDateTime requestAt = LocalDateTime.now();
        Long userId = 0L;
        String accessRole = "ANY";

        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            log.error("[{}] AccessLogFilter error: {}", RequestFlowLogger.getCurrentUUID(), e.getMessage(), e);
            if (!res.isCommitted()) {
                sendJsonError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
            }
        } finally {
            try {
                logAccess(req, res, requestAt, userId, accessRole);
            } catch (Exception logEx) {
                log.warn("접근 로그 처리 중 오류", logEx);
            } finally {
                res.copyBodyToResponse();
            }
        }
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtils.toJson(APIResponse.fail(status, message)));
    }

    private void logAccess(ContentCachingRequestWrapper req,
                           ContentCachingResponseWrapper res,
                           LocalDateTime requestAt,
                           Long userId,
                           String role) {

        LocalDateTime responseAt = LocalDateTime.now();

        String requestBody = safeBody(req.getContentAsByteArray());
        String responseBody = safeBody(res.getContentAsByteArray());

        String headers = Collections.list(req.getHeaderNames()).stream()
                .collect(Collectors.toMap(h -> h, req::getHeader))
                .toString();

        AccessLogRequest accessLog = AccessLogRequest.of(
                userId,
                role,
                req.getMethod(),
                req.getRequestURI(),
                req.getQueryString(),
                requestBody,
                responseBody,
                headers,
                req.getHeader("User-Agent"),
                AccessLogRequest.extractClientIp(req),
                res.getStatus(),
                Thread.currentThread().getName(),
                requestAt,
                responseAt
        );

        log.info("AccessLog : {}\n", JsonUtils.toJson(accessLog));
        rabbitTemplate.convertAndSend("exchange.access.log", "route.access.log.save", accessLog.toCommand());
    }

    private String safeBody(byte[] body) {
        if (body == null || body.length == 0) return "";
        return new String(body, StandardCharsets.UTF_8);
    }
}
