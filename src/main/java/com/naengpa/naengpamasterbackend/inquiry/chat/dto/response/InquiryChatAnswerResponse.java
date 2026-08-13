package com.naengpa.naengpamasterbackend.inquiry.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record InquiryChatAnswerResponse(
        @Schema(description = "챗봇 대화 세션 ID", example = "1")
        Long conversationSessionId,
        @Schema(description = "사용자가 전송한 질문", example = "사전재료 등록 방법을 알려줘")
        String userMessage,
        @Schema(description = "정책 문서 기반 AI 답변", example = "사전재료 등록이 필요하면 관리자에게 문의해 주세요.")
        String answer,
        @Schema(description = "정책 문서로 답변 가능한 질문인지 여부", example = "true")
        boolean answerable,
        @Schema(description = "답변 생성에 사용된 정책 문서 출처", example = "[\"product-policy.md\"]")
        List<String> sources,
        @Schema(description = "답변 생성 시각", example = "2026-08-11T09:00:01")
        LocalDateTime createdAt
) {
}
