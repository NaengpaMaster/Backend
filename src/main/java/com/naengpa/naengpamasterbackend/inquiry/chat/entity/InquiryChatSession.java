package com.naengpa.naengpamasterbackend.inquiry.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_chat_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_chat_session_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static InquiryChatSession create(Long memberId, String title) {
        InquiryChatSession session = new InquiryChatSession();
        session.memberId = memberId;
        session.title = title;
        session.createdAt = LocalDateTime.now();
        session.deleted = false;
        return session;
    }

    public void touch() {
        updatedAt = LocalDateTime.now();
    }
}
