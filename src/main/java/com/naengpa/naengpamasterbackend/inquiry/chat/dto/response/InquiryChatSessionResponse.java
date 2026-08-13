package com.naengpa.naengpamasterbackend.inquiry.chat.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record InquiryChatSessionResponse(
        @Schema(description = "챗봇 대화 세션 ID", example = "1")
        Long conversationSessionId,
        @Schema(description = "대화 제목", example = "사전재료 등록 방법")
        String title,
        @Schema(description = "세션 생성 시각", example = "2026-08-11T09:00:00")
        LocalDateTime createdAt,
        @Schema(description = "최근 대화 시각", example = "2026-08-11T09:05:00")
        LocalDateTime updatedAt
) {
    public static InquiryChatSessionResponse from(InquiryChatSession session) {
        return new InquiryChatSessionResponse(
                session.getId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt()
        );
    }
}
