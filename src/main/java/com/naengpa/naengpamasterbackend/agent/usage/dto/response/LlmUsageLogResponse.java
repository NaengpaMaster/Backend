package com.naengpa.naengpamasterbackend.agent.usage.dto.response;

import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmCallStatus;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LlmUsageLogResponse(
        Long llmUsageLogId,
        String modelName,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        BigDecimal estimatedCost,
        LlmCallStatus status,
        String failureMessage,
        LocalDateTime createdAt
) {

    public static LlmUsageLogResponse from(LlmUsageLog llmUsageLog) {
        return new LlmUsageLogResponse(
                llmUsageLog.getLlmUsageLogId(),
                llmUsageLog.getModelName(),
                llmUsageLog.getPromptTokens(),
                llmUsageLog.getCompletionTokens(),
                llmUsageLog.getTotalTokens(),
                llmUsageLog.getEstimatedCost(),
                llmUsageLog.getStatus(),
                llmUsageLog.getFailureMessage(),
                llmUsageLog.getCreatedAt()
        );
    }
}
