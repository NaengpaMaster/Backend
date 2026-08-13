package com.naengpa.naengpamasterbackend.agent.conversation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// AI 대화방 1개
@Entity
@Table(name = "conversation_sessions")
@Getter
@NoArgsConstructor
public class ConversationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_session_id")
    private Long conversationSessionId;
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    public static ConversationSession create(
            Long memberId,
            String title
    ) {
        ConversationSession conversationSession = new ConversationSession();
        conversationSession.memberId = memberId;
        conversationSession.title = title;
        conversationSession.createdAt = LocalDateTime.now();
        conversationSession.isDeleted = false;
        return conversationSession;
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}