package com.kmong.scheduler;

import com.kmong.config.UnsafeRestTemplateFactory;
import com.kmong.support.properties.EsimProperties;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class OrderApiCall {

    private final EsimProperties esimProperties;
    RestTemplate restTemplate = UnsafeRestTemplateFactory.createUnsafeRestTemplate();
    private final HttpHeaders httpHeaders = new HttpHeaders();

    public Map<String, Object> callApiCompany() {
        String apiUrl = "https://tfmshippingsys.fastmove.com.tw/Api/QuoteMg/myQueryAll";

        Map<String, Object> result = new HashMap<>();
        boolean isSuccess = false;

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchantId", esimProperties.getMerchantId());
            requestBody.put("encStr", esimProperties.getEncStr());

            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, httpHeaders);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, Map.class
            );

            Map<String, Object> body = response.getBody();
            Object productList = body != null ? body.get("prodList") : null;

            if (productList == null) {
                log.warn("[WARN] productList is null — API 요청 실패로 간주");
            } else {
                log.info("[INFO] API 호출 성공");
                isSuccess = true;
            }

            result.put("body", body);
            result.put("isSuccess", isSuccess);

        } catch (Exception e) {
            log.error("[ERROR] API 요청 중 예외 발생: {}", e.getMessage());
            result.put("body", null);
            result.put("isSuccess", false);
        }

        return result;
    }

    public Map<String, Object> callApiRedeem() {
        String apiUrl = "https://tfmshippingsys.fastmove.com.tw/Api/SOrder/mybuyesimRedemption";
        Map<String, Object> result = new HashMap<>();

        try {
            String merchantId = "b000070";
            String deptId = "000086";
            String qrcodeType = "2";
            String wmproductId = "WM_000004";
            String qty = "2";
            String token = "a31f84cd66b7a898ae442b7d7799e29f";

            String encStr = generateEncStr(merchantId, deptId, qrcodeType, wmproductId, qty, token);

            Map<String, Object> body = Map.of(
                    "merchantId", merchantId,
                    "deptId", deptId,
                    "qrcodeType", Integer.parseInt(qrcodeType),
                    "prodList", List.of(Map.of("wmproductId", wmproductId, "qty", Integer.parseInt(qty))),
                    "encStr", encStr
            );

            log.info("[REQ eSIM] {}", JsonUtils.toJson(body));

            ResponseEntity<Map> response = postJsonRequest(apiUrl, body);
            Map<String, Object> resBody = response.getBody();
            log.info("[RES eSIM] {}", JsonUtils.toJson(resBody));

            result.put("body", resBody);
            result.put("orderId", resBody != null ? resBody.get("orderId") : null);
            result.put("isSuccess", resBody != null && Objects.equals(resBody.get("code"), 0));

        } catch (Exception e) {
            log.error("[ERROR] eSIM API 호출 실패: {}", e.getMessage(), e);
            result.put("isSuccess", false);
        }

        return result;
    }

    /** === SHA1 해시 계산 === */
    private String generateEncStr(String merchantId, String deptId, String qrcodeType,
                                  String wmproductId, String qty, String token) {
        String raw = merchantId + deptId + qrcodeType + wmproductId + qty + token;
        return DigestUtils.sha1Hex(raw).toUpperCase();
    }

    /** === 공통 POST 요청 유틸 === */
    private ResponseEntity<Map> postJsonRequest(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
    }

}
