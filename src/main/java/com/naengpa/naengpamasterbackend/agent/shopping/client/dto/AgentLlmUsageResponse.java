package com.naengpa.naengpamasterbackend.agent.shopping.client.dto;

import java.math.BigDecimal;

public record AgentLlmUsageResponse(
        String modelName,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        BigDecimal estimatedCost
) {
}
