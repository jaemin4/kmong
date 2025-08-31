package com.kmong.config;

import com.kmong.support.properties.CoolSmsProperties;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CoolSmsConfig {
    private final CoolSmsProperties coolSmsProperties;

    @Bean
    public DefaultMessageService defaultMessageService() {
        return NurigoApp.INSTANCE.initialize(
                coolSmsProperties.getKey(),
                coolSmsProperties.getSecret(),
                "https://api.coolsms.co.kr"
        );
    }

}
