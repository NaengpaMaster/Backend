package com.naengpa.naengpamasterbackend.agent.usage.dto.response;

import com.naengpa.naengpamasterbackend.agent.usage.repository.LlmUsageSummaryProjection;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public record AdminLlmUsageLogPageResponse(
        List<AdminLlmUsageLogResponse> content,
        int totalPages,
        long totalElements,
        long successCount,
        long failedCount,
        long totalTokens,
        BigDecimal totalEstimatedCost
) {
    public static AdminLlmUsageLogPageResponse from(
            Page<AdminLlmUsageLogResponse> page,
            LlmUsageSummaryProjection summary
    ) {
        return new AdminLlmUsageLogPageResponse(
                page.getContent(),
                page.getTotalPages(),
                summary.getTotalCount(),
                summary.getSuccessCount(),
                summary.getFailedCount(),
                summary.getTotalTokens(),
                summary.getTotalEstimatedCost()
        );
    }
}
