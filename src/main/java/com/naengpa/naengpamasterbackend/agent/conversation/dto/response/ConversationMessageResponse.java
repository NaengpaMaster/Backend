package com.naengpa.naengpamasterbackend.agent.conversation.dto.response;

import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessage;
import com.naengpa.naengpamasterbackend.agent.conversation.entity.ConversationMessageRole;
import java.time.LocalDateTime;

public record ConversationMessageResponse(
        Long conversationMessageId,
        Long conversationSessionId,
        ConversationMessageRole role,
        String content,
        LocalDateTime createdAt
) {
    public static ConversationMessageResponse from(ConversationMessage message) {
        return new ConversationMessageResponse(
                message.getConversationMessageId(),
                message.getConversationSessionId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
