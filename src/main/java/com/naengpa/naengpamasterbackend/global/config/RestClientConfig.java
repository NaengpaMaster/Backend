package com.naengpa.naengpamasterbackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        // 외부 HTTP API 호출용 RestClient Builder를 Bean으로 등록
        // Agent 서버뿐 아니라 이후 외부 API 연동에서도 재사용 가능
        return RestClient.builder();
    }
}
