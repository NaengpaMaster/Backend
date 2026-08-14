package com.naengpa.naengpamasterbackend.agent.shopping.client;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentShoppingRecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AgentShoppingRecommendationClient {

    private final RestClient restClient;

    public AgentShoppingRecommendationClient(
            RestClient.Builder restClientBuilder,
            @Value("${agent.base-url}") String agentBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(agentBaseUrl)
                .build();
    }

    public AgentShoppingRecommendationResponse recommend(
            AgentShoppingRecommendationRequest request
    ) {
        long startedAt = System.nanoTime();

        // Spring Boot 백엔드가 FastAPI Agent 서버의 추천 API를 호출하는 지점
        try {
            return restClient.post()
                    .uri("/agent/v1/shopping/recommendations")
                    .body(request)
                    .retrieve()
                    .body(AgentShoppingRecommendationResponse.class);
        } finally {
            log.info(
                    "장보기 Agent 호출 완료 - elapsedMs={}, candidateCount={}, limit={}",
                    (System.nanoTime() - startedAt) / 1_000_000,
                    request.candidateProducts().size(),
                    request.limit()
            );
        }
    }
}
