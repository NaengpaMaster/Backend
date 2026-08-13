package com.naengpa.naengpamasterbackend.agent.shopping.dto.request;

import java.util.List;

public record ShoppingRecommendationRequest(
        Integer limit,
        List<Long> excludeProductIds,
        Long fridgeId
) {

    public ShoppingRecommendationRequest(Integer limit) {
        this(limit, List.of(), null);
    }

    public ShoppingRecommendationRequest {
        excludeProductIds = excludeProductIds == null ? List.of() : excludeProductIds;
    }
}
