package com.naengpa.naengpamasterbackend.inquiry.chat.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatSession;

import java.time.LocalDateTime;

public record InquiryChatSessionResponse(
        Long conversationSessionId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InquiryChatSessionResponse from(InquiryChatSession session) {
        return new InquiryChatSessionResponse(
                session.getId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt()
        );
    }
}
