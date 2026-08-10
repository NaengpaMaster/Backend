package com.naengpa.naengpamasterbackend.agent.usage.dto.response;

import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmCallStatus;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmFeatureType;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import com.naengpa.naengpamasterbackend.member.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminLlmUsageLogResponse(
        Long llmUsageLogId,
        Long memberId,
        String email,
        String nickname,
        LlmFeatureType featureType,
        String modelName,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        BigDecimal estimatedCost,
        LlmCallStatus status,
        String failureMessage,
        LocalDateTime createdAt
) {

    public static AdminLlmUsageLogResponse from(LlmUsageLog llmUsageLog, Member member) {
        return new AdminLlmUsageLogResponse(
                llmUsageLog.getLlmUsageLogId(),
                llmUsageLog.getMemberId(),
                member == null ? null : member.getEmail(),
                member == null ? null : member.getNickname(),
                llmUsageLog.getFeatureType(),
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
