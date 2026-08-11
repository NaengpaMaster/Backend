package com.naengpa.naengpamasterbackend.inquiry.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_chat_message_id")
    private Long id;

    @Column(name = "inquiry_chat_session_id", nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryChatMessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static InquiryChatMessage create(Long sessionId, InquiryChatMessageRole role, String content) {
        InquiryChatMessage message = new InquiryChatMessage();
        message.sessionId = sessionId;
        message.role = role;
        message.content = content;
        message.createdAt = LocalDateTime.now();
        return message;
    }
}
