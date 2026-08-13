package com.naengpa.naengpamasterbackend.agent.shopping.client.dto;

import java.util.List;

public record AgentShoppingRecommendationResponse(
        List<AgentShoppingRecommendationItemResponse> items,
        AgentLlmUsageResponse usage
) {
}
