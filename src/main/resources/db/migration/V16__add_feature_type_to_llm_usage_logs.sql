ALTER TABLE llm_usage_logs
    ADD COLUMN feature_type VARCHAR(30);

UPDATE llm_usage_logs
SET feature_type = 'SHOPPING_RECOMMENDATION'
WHERE feature_type IS NULL;

ALTER TABLE llm_usage_logs
    ALTER COLUMN feature_type SET NOT NULL;

ALTER TABLE llm_usage_logs
    ADD CONSTRAINT chk_llm_usage_logs_feature_type
        CHECK (feature_type IN (
            'SHOPPING_RECOMMENDATION',
            'INQUIRY_QNA'
        ));

CREATE INDEX idx_llm_usage_logs_feature_created_at
    ON llm_usage_logs(feature_type, created_at DESC);

