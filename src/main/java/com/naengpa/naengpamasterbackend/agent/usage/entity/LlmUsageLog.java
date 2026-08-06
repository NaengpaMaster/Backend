package com.naengpa.naengpamasterbackend.agent.usage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "llm_usage_logs")
@Getter
@NoArgsConstructor
public class LlmUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "llm_usage_log_id")
    private Long llmUsageLogId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "prompt_tokens", nullable = false)
    private Integer promptTokens;

    @Column(name = "completion_tokens", nullable = false)
    private Integer completionTokens;

    @Column(name = "total_tokens", nullable = false)
    private Integer totalTokens;

    @Column(name = "estimated_cost", nullable = false, precision = 12, scale = 6)
    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LlmCallStatus status;

    @Column(name = "failure_message")
    private String failureMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static LlmUsageLog success(
            Long memberId,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            BigDecimal estimatedCost
    ) {
        LlmUsageLog llmUsageLog = new LlmUsageLog();
        llmUsageLog.memberId = memberId;
        llmUsageLog.modelName = modelName;
        llmUsageLog.promptTokens = promptTokens;
        llmUsageLog.completionTokens = completionTokens;
        llmUsageLog.totalTokens = totalTokens;
        llmUsageLog.estimatedCost = estimatedCost;
        llmUsageLog.status = LlmCallStatus.SUCCESS;
        llmUsageLog.failureMessage = null;
        return llmUsageLog;
    }

    public static LlmUsageLog failed(
            Long memberId,
            String modelName,
            String failureMessage
    ) {
        LlmUsageLog llmUsageLog = new LlmUsageLog();
        llmUsageLog.memberId = memberId;
        llmUsageLog.modelName = modelName;
        llmUsageLog.promptTokens = 0;
        llmUsageLog.completionTokens = 0;
        llmUsageLog.totalTokens = 0;
        llmUsageLog.estimatedCost = BigDecimal.ZERO;
        llmUsageLog.status = LlmCallStatus.FAILED;
        llmUsageLog.failureMessage = failureMessage;
        return llmUsageLog;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
