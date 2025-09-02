package com.kmong.interfaces.auth;

import com.kmong.infra.naver.NaverAPIClient;
import com.kmong.support.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class NaverOrderController {

    private final NaverAPIClient naverAPIClient;

    @GetMapping("/token")
    public APIResponse<String> getToken() {
        String token = naverAPIClient.getToken();
        return APIResponse.success(token);
    }

    @GetMapping("/order")
    public APIResponse<List<Map<String, Object>>>   getOrder() {
        String from = "2025-09-01T00:00:00.000+09:00";
        String to   = "2025-09-01T23:59:59.999+09:00";
        String accessToken = naverAPIClient.getToken();

        List<Map<String,Object>> data = naverAPIClient.fetchOrders(accessToken, from, to);
        return APIResponse.success(data);
    }



}
