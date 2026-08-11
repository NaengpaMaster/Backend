package com.naengpa.naengpamasterbackend.inquiry.chat.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessage;
import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessageRole;

import java.time.LocalDateTime;

public record InquiryChatMessageResponse(
        Long messageId,
        InquiryChatMessageRole role,
        String content,
        LocalDateTime createdAt
) {
    public static InquiryChatMessageResponse from(InquiryChatMessage message) {
        return new InquiryChatMessageResponse(
                message.getId(), message.getRole(), message.getContent(), message.getCreatedAt()
        );
    }
}
