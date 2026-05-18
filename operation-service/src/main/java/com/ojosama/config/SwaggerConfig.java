package com.ojosama.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Festie Operation Service API")
                        .description("Festie 운영 관리 서비스 API 문서입니다. <br>" +
                                "공지사항, 신고, 블랙리스트, 운영 요청 등의 기능을 제공합니다.")
                        .version("1.0.0"));
    }
}
