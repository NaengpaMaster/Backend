-- =========================================
-- 회원 상태 변경 이력
-- =========================================
CREATE TABLE member_status_histories (
                                         member_status_history_id BIGSERIAL PRIMARY KEY,

                                         member_id BIGINT NOT NULL,

                                         previous_status VARCHAR(20) NOT NULL
                                             CHECK (previous_status IN ('ACTIVE', 'INACTIVE')),

                                         changed_status VARCHAR(20) NOT NULL
                                             CHECK (changed_status IN ('ACTIVE', 'INACTIVE')),

                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE member_status_histories IS '회원 상태 변경 이력';

CREATE INDEX idx_member_status_histories_member_created
    ON member_status_histories(member_id, created_at DESC);

CREATE INDEX idx_member_status_histories_status_created
    ON member_status_histories(changed_status, created_at DESC);
