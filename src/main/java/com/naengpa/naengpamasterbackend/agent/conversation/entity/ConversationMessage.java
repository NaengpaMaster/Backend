package com.naengpa.naengpamasterbackend.agent.conversation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_messages")
@Getter
@NoArgsConstructor
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_message_id")
    private Long conversationMessageId;

    @Column(name = "conversation_session_id", nullable = false)
    private Long conversationSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ConversationMessageRole role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ConversationMessage create(
            Long conversationSessionId,
            ConversationMessageRole role,
            String content
    ) {
        ConversationMessage conversationMessage = new ConversationMessage();
        conversationMessage.conversationSessionId = conversationSessionId;
        conversationMessage.role = role;
        conversationMessage.content = content;
        conversationMessage.createdAt = LocalDateTime.now();
        return conversationMessage;
    }
}