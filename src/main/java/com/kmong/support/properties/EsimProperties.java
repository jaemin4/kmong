package com.kmong.support.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.esim")
public class EsimProperties {
    private String merchantId;
    private String token;
    private String encStr;
}
