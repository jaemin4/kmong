package com.kmong.support.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sms.cool")
public class CoolSmsProperties {
    private String key;
    private String secret;
    private String number;
}
