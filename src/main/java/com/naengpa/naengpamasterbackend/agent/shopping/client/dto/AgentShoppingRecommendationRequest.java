package com.naengpa.naengpamasterbackend.agent.shopping.client.dto;

import java.util.List;

public record AgentShoppingRecommendationRequest(
        Integer limit,
        List<String> favoriteFoods,
        List<AgentProductPayload> fridgeItems,
        List<AgentProductPayload> shoppingItems,
        List<AgentProductPayload> candidateProducts
) {
}
