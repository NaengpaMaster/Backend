package com.naengpa.naengpamasterbackend.inquiry.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record InquiryChatAnswerResponse(
        Long conversationSessionId,
        String userMessage,
        String answer,
        boolean answerable,
        List<String> sources,
        LocalDateTime createdAt
) {
}
