package com.naengpa.naengpamasterbackend.agent.usage.repository;

import java.math.BigDecimal;

public interface LlmUsageSummaryProjection {
    Long getTotalCount();
    Long getSuccessCount();
    Long getFailedCount();
    Long getTotalTokens();
    BigDecimal getTotalEstimatedCost();
}
