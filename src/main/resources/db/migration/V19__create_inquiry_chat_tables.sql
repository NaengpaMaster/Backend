CREATE TABLE inquiry_chat_sessions (
    inquiry_chat_session_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_inquiry_chat_sessions_member
        FOREIGN KEY (member_id)
        REFERENCES members(member_id)
);

CREATE TABLE inquiry_chat_messages (
    inquiry_chat_message_id BIGSERIAL PRIMARY KEY,
    inquiry_chat_session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inquiry_chat_messages_session
        FOREIGN KEY (inquiry_chat_session_id)
        REFERENCES inquiry_chat_sessions(inquiry_chat_session_id),

    CONSTRAINT chk_inquiry_chat_messages_role
        CHECK (role IN ('USER', 'ASSISTANT'))
);

COMMENT ON TABLE inquiry_chat_sessions IS '문의 Q&A 챗봇 대화 세션';
COMMENT ON TABLE inquiry_chat_messages IS '문의 Q&A 챗봇 대화 메시지';

CREATE INDEX idx_inquiry_chat_sessions_member_created
    ON inquiry_chat_sessions(member_id, created_at DESC);

CREATE INDEX idx_inquiry_chat_messages_session_created
    ON inquiry_chat_messages(inquiry_chat_session_id, created_at);

