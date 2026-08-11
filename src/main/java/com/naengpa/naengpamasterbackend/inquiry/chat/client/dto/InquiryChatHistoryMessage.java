package com.naengpa.naengpamasterbackend.inquiry.chat.client.dto;

import com.naengpa.naengpamasterbackend.inquiry.chat.entity.InquiryChatMessage;

public record InquiryChatHistoryMessage(
        String role,
        String content
) {
    public static InquiryChatHistoryMessage from(InquiryChatMessage message) {
        return new InquiryChatHistoryMessage(message.getRole().name(), message.getContent());
    }
}
