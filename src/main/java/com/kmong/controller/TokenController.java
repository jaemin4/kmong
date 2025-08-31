package com.kmong.controller;

import com.kmong.service.NaverTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
