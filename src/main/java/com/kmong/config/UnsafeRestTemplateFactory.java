package com.kmong.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

public class UnsafeRestTemplateFactory {

    public static RestTemplate createUnsafeRestTemplate() {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            var socketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            HttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);

            return new RestTemplate(factory);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsafe RestTemplate", e);
        }
    }
}
