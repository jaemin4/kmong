package com.kmong.domain.auth;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class NaverTokenService {

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
