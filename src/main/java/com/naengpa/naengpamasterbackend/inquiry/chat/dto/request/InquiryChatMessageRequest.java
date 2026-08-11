package com.naengpa.naengpamasterbackend.inquiry.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryChatMessageRequest(
        Long conversationSessionId,
        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 2000, message = "질문 내용은 2000자 이하여야 합니다.")
        String content
) {
}
