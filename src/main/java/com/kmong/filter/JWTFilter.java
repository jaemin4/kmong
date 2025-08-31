package com.kmong.filter;

import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.domain.user.User;
import com.kmong.domain.user.UserRepository;
import com.kmong.infra.user.CustomUserDetails;
import com.kmong.support.response.APIResponse;
import com.kmong.support.utils.JWTUtil;
import com.kmong.support.utils.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/healthz");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        LocalDateTime requestAt = LocalDateTime.now();
        String uri = request.getRequestURI();
        String accessRole = "ANY";
        Long userId = null;

        try {
            if (isExcludedPath(uri)) {
                filterChain.doFilter(req, res);
                return;
            }

            String token = extractToken(req);
            if (token == null || !jwtUtil.validateToken(token)) {
                filterChain.doFilter(req, res);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(req, res);
                return;
            }

            String userIdStr = jwtUtil.getUsername(token);
            accessRole = jwtUtil.getRole(token);

            try {
                userId = Long.parseLong(userIdStr);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("인증에 실패하였습니다"));

                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info("[{}] authentication success : {},{}", RequestFlowLogger.getCurrentUUID(), userId, accessRole);
                filterChain.doFilter(req, res);

            } catch (AuthenticationException | NumberFormatException e) {
                log.warn("[{}] authentication failed: {}", RequestFlowLogger.getCurrentUUID(), e.getMessage());
                sendJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "잘못된 사용자 정보입니다.");
            }

        } catch (Exception e) {
            log.error("[{}] JWTFilter unexpected error: {}", RequestFlowLogger.getCurrentUUID(), e.getMessage(), e);
            sendJsonError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
        } finally {
            try {
                logAccess(req, res, requestAt, userId == null ? 0L : userId, accessRole);
                res.copyBodyToResponse();
            } catch (Exception e) {
                log.warn("응답 로그 처리 중 오류 발생", e);
            }
        }
    }

    private boolean isExcludedPath(String uri) {
        return uri.equals("/auth/login");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtils.toJson(APIResponse.fail(status, message)));
    }

    private void logAccess(ContentCachingRequestWrapper req, ContentCachingResponseWrapper res, LocalDateTime requestAt,
                           Long userId, String role) {

        LocalDateTime responseAt = LocalDateTime.now();

        String requestBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
        String responseBody = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8);

        String headers = Collections.list(req.getHeaderNames()).stream()
                .collect(Collectors.toMap(h -> h, req::getHeader))
                .toString();

        AccessLogRequest accessLog = AccessLogRequest.of(
                userId, role, req.getMethod(), req.getRequestURI(), req.getQueryString(), requestBody,
                responseBody, headers, req.getHeader("User-Agent"), AccessLogRequest.extractClientIp(req),
                res.getStatus(), Thread.currentThread().getName(), requestAt, responseAt
        );

        log.info("AccessLog : {}\n", JsonUtils.toJson(accessLog));
        rabbitTemplate.convertAndSend("exchange.access.log", "route.access.log.save", accessLog.toCommand());
    }
}
