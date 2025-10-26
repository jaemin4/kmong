package com.kmong.infra.naver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmong.support.properties.NaverApiProperties;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.kmong.support.utils.RestUtils.createConnection;
import static com.kmong.support.utils.RestUtils.readResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverAPIClient {

    private static final String BASE_URL = "https://api.commerce.naver.com/external/v1";
    private final ObjectMapper mapper = new ObjectMapper();
    private final NaverApiProperties naverApiProperties;

    /** 주문 조회 */
    public List<Map<String, Object>> fetchOrders(String accessToken, String from, String to) {
        String urlStr =  BASE_URL + "/pay-order/seller/product-orders"
                + "?from=" + URLEncoder.encode(from, StandardCharsets.UTF_8)
                + "&to=" + URLEncoder.encode(to, StandardCharsets.UTF_8)
                + "&rangeType=PAYED_DATETIME";
        try {
            HttpURLConnection conn = createConnection(urlStr, "GET", accessToken);
            String response = readResponse(conn);
            conn.disconnect();

            JsonNode contents = mapper.readTree(response)
                    .path("data")
                    .path("contents");

            List<Map<String,Object>> data = mapper.convertValue(contents, new TypeReference<>() {});
            log.info(JsonUtils.toJson(data));

            return data;
        } catch (Exception e) {
            log.error("NAVER fetchOrders failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /** 토큰 발급 */
    public String getToken() {
        try {
            String clientId = naverApiProperties.getClientId();
            String clientSecret = naverApiProperties.getClientSecret();
            long timestamp = System.currentTimeMillis();

            String password = clientId + "_" + timestamp;
            String hashed = BCrypt.hashpw(password, clientSecret);
            String clientSecretSign = Base64.getEncoder()
                    .encodeToString(hashed.getBytes(StandardCharsets.UTF_8));

            String formData = String.format(
                    "grant_type=client_credentials&client_id=%s&timestamp=%d&client_secret_sign=%s&type=SELF",
                    clientId, timestamp, clientSecretSign
            );

            HttpURLConnection conn = createConnection(BASE_URL + "/oauth2/token", "POST", null);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(formData.getBytes(StandardCharsets.UTF_8));
            }

            String response = readResponse(conn);
            conn.disconnect();

            log.info("accessToken : {}", response);

            return new JSONObject(response).optString("access_token", null);
        } catch (Exception e) {
            log.error("NAVER getToken failed: {}", e.getMessage(), e);
            return null;
        }
    }

}
