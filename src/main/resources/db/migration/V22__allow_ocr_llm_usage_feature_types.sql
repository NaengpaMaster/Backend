ALTER TABLE llm_usage_logs
    DROP CONSTRAINT IF EXISTS chk_llm_usage_logs_feature_type;

ALTER TABLE llm_usage_logs
    ADD CONSTRAINT chk_llm_usage_logs_feature_type
        CHECK (feature_type IN (
            'SHOPPING_RECOMMENDATION',
            'INQUIRY_QNA',
            'RECEIPT_OCR',
            'FRIDGE_PHOTO_ANALYSIS'
        ));
