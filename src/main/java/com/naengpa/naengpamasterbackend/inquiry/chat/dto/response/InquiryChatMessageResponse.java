package com.naengpa.naengpamasterbackend.inquiry.chat.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessage;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessageRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record InquiryChatMessageResponse(
        @Schema(description = "챗봇 메시지 ID", example = "1")
        Long messageId,
        @Schema(description = "메시지 작성 역할", example = "USER")
        InquiryChatMessageRole role,
        @Schema(description = "메시지 내용", example = "사전재료 등록 방법을 알려줘")
        String content,
        @Schema(description = "메시지 생성 시각", example = "2026-08-11T09:00:00")
        LocalDateTime createdAt
) {
    public static InquiryChatMessageResponse from(InquiryChatMessage message) {
        return new InquiryChatMessageResponse(
                message.getId(), message.getRole(), message.getContent(), message.getCreatedAt()
        );
    }
}
