package com.naengpa.naengpamasterbackend.agent.shopping.client;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentShoppingRecommendationRequest;
import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentShoppingRecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        // Spring Boot 백엔드가 FastAPI Agent 서버의 추천 API를 호출하는 지점
        return restClient.post()
                .uri("/agent/v1/shopping/recommendations")
                .body(request)
                .retrieve()
                .body(AgentShoppingRecommendationResponse.class);
    }
}
