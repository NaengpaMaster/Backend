package com.naengpa.naengpamasterbackend.agent.conversation.dto.response;

import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationSession;

import java.time.LocalDateTime;

public record ConversationSessionResponse(
        Long conversationSessionId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static ConversationSessionResponse from(ConversationSession session) {
        return new ConversationSessionResponse(
                session.getConversationSessionId(),
                session.getTitle(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
