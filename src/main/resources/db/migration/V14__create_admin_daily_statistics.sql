-- =========================================
-- 관리자 일별 통계 집계 및 실행 상태
-- =========================================
CREATE TABLE IF NOT EXISTS admin_daily_statistics (
    admin_daily_statistics_id BIGSERIAL PRIMARY KEY,
    statistics_date DATE NOT NULL,

    new_member_count BIGINT NOT NULL DEFAULT 0,
    inactive_member_count BIGINT NOT NULL DEFAULT 0,
    registered_material_count BIGINT NOT NULL DEFAULT 0,
    expired_material_count BIGINT NOT NULL DEFAULT 0,
    created_recipe_count BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILED')),
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message VARCHAR(500),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_admin_daily_statistics_date
        UNIQUE (statistics_date)
);

COMMENT ON TABLE admin_daily_statistics IS '관리자 일별 통계 집계 및 실행 상태';

CREATE INDEX IF NOT EXISTS idx_admin_daily_statistics_date
    ON admin_daily_statistics(statistics_date DESC);
