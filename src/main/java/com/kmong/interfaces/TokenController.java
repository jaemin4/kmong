package com.kmong.interfaces;

import com.kmong.domain.auth.NaverTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private final NaverTokenService naverTokenService;

    public TokenController(NaverTokenService naverTokenService) {
        this.naverTokenService = naverTokenService;
    }

    @GetMapping("/token")
    public String getToken() {
        return naverTokenService.getToken();
    }
}
