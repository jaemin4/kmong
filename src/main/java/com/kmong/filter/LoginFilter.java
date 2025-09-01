package com.kmong.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmong.domain.user.User;
import com.kmong.domain.user.UserRepository;
import com.kmong.infra.user.CustomUserDetails;
import com.kmong.support.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            Map<String, String> requestMap = new ObjectMapper().readValue(request.getInputStream(), Map.class);
            String email = requestMap.get("email");
            String password = requestMap.get("password");

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(email, password);

            return authenticationManager.authenticate(token);

        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {

        CustomUserDetails details = (CustomUserDetails) authResult.getPrincipal();
        User user = details.getUser();

        String email = user.getEmail();
        String role = user.getRole().toString();
        String userId = user.getId().toString();

        // Access Token (30일 임시)
        String accessToken = jwtUtil.createJwt(userId, role, 1000 * 60 * 60L * 24 * 30);

        // Refresh Token (28일)
        String refreshToken = jwtUtil.createJwt(userId, role, 1000L * 60 * 60 * 24 * 7 * 4);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        new ObjectMapper().writeValue(response.getWriter(), new AuthResponse(accessToken, refreshToken));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("로그인 실패: " + failed.getMessage());
    }
}
