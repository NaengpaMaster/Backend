package com.naengpa.naengpamasterbackend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // Swagger UI에서 JWT를 입력해 인증이 필요한 API를 호출할 수 있도록 Bearer 인증 방식을 설정합니다.
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().components(new Components().addSecuritySchemes(
                "bearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
        ));
    }
}
