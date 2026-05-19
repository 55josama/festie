package com.ojosama.eventservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Festie Event Service API")
                        .description("행사 관리 서비스 API 명세")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-User-Id")
                        .addList("X-User-Role"))
                .components(new Components()
                        .addSecuritySchemes("X-User-Id", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")
                                .description("게이트웨이에서 주입되는 사용자 UUID"))
                        .addSecuritySchemes("X-User-Role", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Role")
                                .description("사용자 역할 (ADMIN, CONCERT_MANAGER, FESTIVAL_MANAGER, FANMEETING_MANAGER, POPUP_MANAGER, USER)")));
    }
}
