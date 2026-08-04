package com.naengpa.naengpamasterbackend.agent.shopping.dto.response;

import java.util.List;

public record ShoppingRecommendationResponse (
        List<ShoppingRecommendationItemResponse> items
){
}
