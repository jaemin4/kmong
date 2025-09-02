package com.kmong.infra.naver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class NaverAPIClient {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> fetchOrders(String accessToken, String from, String to) {
        HttpURLConnection conn = null;
        try {
            String urlStr = "https://api.commerce.naver.com/external/v1/pay-order/seller/product-orders"
                    + "?from=" + URLEncoder.encode(from, StandardCharsets.UTF_8)
                    + "&to=" + URLEncoder.encode(to, StandardCharsets.UTF_8)
                    + "&rangeType=PAYED_DATETIME";

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                for (String line; (line = br.readLine()) != null; ) {
                    sb.append(line);
                }
            }

            // 문자열 → JSON 파싱
            JsonNode root = mapper.readTree(sb.toString());
            JsonNode contents = root.path("data").path("contents");

            // contents 배열을 List<Map> 으로 변환
            return mapper.convertValue(contents, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }


    public String getToken() {
        try {
            String clientId = "75Z8MUbYcw3vpvbV0F9Ux4";
            String clientSecret = "$2a$04$7WLDVDCaxCC3Vt609oHN.O"; // bcrypt salt
            long timestamp = System.currentTimeMillis();
            String password = clientId + "_" + timestamp;

            String hashed = BCrypt.hashpw(password, clientSecret);
            String clientSecretSign = Base64.getEncoder()
                    .encodeToString(hashed.getBytes(StandardCharsets.UTF_8));

            String urlStr = "https://api.commerce.naver.com/external/v1/oauth2/token";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String formData = "grant_type=client_credentials"
                    + "&client_id=" + clientId
                    + "&timestamp=" + timestamp
                    + "&client_secret_sign=" + clientSecretSign
                    + "&type=SELF";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(formData.getBytes(StandardCharsets.UTF_8));
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }

            JSONObject json = new JSONObject(response.toString());
            return json.optString("access_token", null);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
