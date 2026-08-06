CREATE TABLE llm_usage_logs (
    llm_usage_log_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens INTEGER NOT NULL DEFAULT 0,
    estimated_cost NUMERIC(12, 6) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    failure_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_llm_usage_logs_member
        FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    CONSTRAINT chk_llm_usage_logs_status
        CHECK (status IN ('SUCCESS', 'FAILED'))
);

CREATE INDEX idx_llm_usage_logs_member_id
    ON llm_usage_logs(member_id);

CREATE INDEX idx_llm_usage_logs_member_created_at
    ON llm_usage_logs(member_id, created_at DESC);
