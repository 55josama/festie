package com.ojosama.userservice.global.config;

import com.ojosama.userservice.global.properties.CoolSmsProperties;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfig {

    @Bean
    public DefaultMessageService defaultMessageService(CoolSmsProperties coolSmsProperties) {
        return NurigoApp.INSTANCE.initialize(
                coolSmsProperties.apiKey(),
                coolSmsProperties.apiSecret(),
                "https://api.coolsms.co.kr"
        );
    }
}