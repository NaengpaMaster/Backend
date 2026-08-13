package com.naengpa.naengpamasterbackend.inquiry.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryChatMessageRequest(
        @Schema(description = "이어갈 대화 세션 ID. 새 대화를 시작할 때는 생략합니다.", example = "1", nullable = true)
        Long conversationSessionId,
        @Schema(description = "챗봇 질문 내용", example = "사전재료 등록 방법을 알려줘", maxLength = 2000)
        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 2000, message = "질문 내용은 2000자 이하여야 합니다.")
        String content
) {
}
