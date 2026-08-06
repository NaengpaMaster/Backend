-- AI 대화방/세션 단위
-- 1번 세션: 장보기 추천 대화
CREATE TABLE IF NOT EXISTS conversation_sessions (
    conversation_session_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,

    CONSTRAINT fk_conversation_sessions_member
        FOREIGN KEY (member_id)
        REFERENCES members(member_id)
);

-- 세션 안에 쌓이는 사용자/AI 메시지
-- USER: 냉장고 보고 장보기 추천해줘
-- ASSISTANT: 감자, 양파, 계란을 추천합니다
CREATE TABLE IF NOT EXISTS conversation_messages (
    conversation_message_id BIGSERIAL PRIMARY KEY,
    conversation_session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_messages_session
        FOREIGN KEY (conversation_session_id)
        REFERENCES conversation_sessions(conversation_session_id)
);

CREATE INDEX IF NOT EXISTS idx_conversation_sessions_member_id
    ON conversation_sessions(member_id);

CREATE INDEX IF NOT EXISTS idx_conversation_messages_session_id
    ON conversation_messages(conversation_session_id);
