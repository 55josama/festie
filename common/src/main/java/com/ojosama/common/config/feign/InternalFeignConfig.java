package com.ojosama.common.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class InternalFeignConfig {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Bean
    public RequestInterceptor internalApiRequestInterceptor(
            @Value("${internal.api.token:${INTERNAL_API_TOKEN:}}") String internalApiToken
    ) {
        return new InternalApiRequestInterceptor(internalApiToken);
    }

    private static final class InternalApiRequestInterceptor implements RequestInterceptor {

        private final String internalApiToken;

        private InternalApiRequestInterceptor(String internalApiToken) {
            this.internalApiToken = internalApiToken;
        }

        @Override
        public void apply(RequestTemplate template) {
            if (!StringUtils.hasText(internalApiToken)) {
                return;
            }
            template.header(INTERNAL_TOKEN_HEADER, internalApiToken);
        }
    }
}
