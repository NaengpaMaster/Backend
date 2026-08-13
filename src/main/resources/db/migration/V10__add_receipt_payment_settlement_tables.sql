-- =========================================================
-- Receipt Agent + TossPayments + Monthly Settlement tables
-- =========================================================

-- 영수증 이미지 1장에 대한 OCR/분석 요청 단위
CREATE TABLE IF NOT EXISTS receipt_analysis (
    receipt_analysis_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    original_file_name VARCHAR(255) NULL,
    s3_object_key VARCHAR(500) NULL,
    image_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    raw_ocr_text TEXT NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_receipt_analysis_member
        FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    CONSTRAINT chk_receipt_analysis_status
        CHECK (status IN ('PENDING', 'REGISTERED', 'REJECTED'))
);

-- OCR로 추출된 영수증 상품 후보 항목
CREATE TABLE IF NOT EXISTS receipt_analysis_items (
    receipt_analysis_item_id BIGSERIAL PRIMARY KEY,
    receipt_analysis_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    extracted_name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NULL,
    matched_product_name VARCHAR(255) NULL,
    quantity VARCHAR(50) NOT NULL DEFAULT '1개',
    expiry_date DATE NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    memo VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_receipt_analysis_items_analysis
        FOREIGN KEY (receipt_analysis_id)
        REFERENCES receipt_analysis(receipt_analysis_id),

    CONSTRAINT fk_receipt_analysis_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(product_id),

    CONSTRAINT chk_receipt_analysis_items_status
        CHECK (status IN ('PENDING', 'REGISTERED', 'REJECTED'))
);

-- TossPayments 자동결제를 위한 빌링키
CREATE TABLE IF NOT EXISTS billing_keys (
    billing_key_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    toss_customer_key VARCHAR(255) NOT NULL,
    toss_billing_key VARCHAR(255) NOT NULL,
    card_company VARCHAR(100) NULL,
    card_number_masked VARCHAR(50) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deactivated_at TIMESTAMP NULL,

    CONSTRAINT fk_billing_keys_member
        FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    CONSTRAINT uq_billing_keys_toss_customer_key
        UNIQUE (toss_customer_key),

    CONSTRAINT uq_billing_keys_toss_billing_key
        UNIQUE (toss_billing_key)
);

-- 월간/연간 구독 결제 내역
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    billing_key_id BIGINT NULL,
    toss_payment_key VARCHAR(255) NULL,
    order_id VARCHAR(255) NOT NULL,
    order_name VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL,
    plan_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'READY',
    billing_period_start DATE NULL,
    billing_period_end DATE NULL,
    approved_at TIMESTAMP NULL,
    failed_reason TEXT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_failed_at TIMESTAMP NULL,
    next_retry_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_payments_member
        FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    CONSTRAINT fk_payments_billing_key
        FOREIGN KEY (billing_key_id)
        REFERENCES billing_keys(billing_key_id),

    CONSTRAINT uq_payments_order_id
        UNIQUE (order_id),

    CONSTRAINT uq_payments_toss_payment_key
        UNIQUE (toss_payment_key),

    CONSTRAINT chk_payments_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payments_plan_type
        CHECK (plan_type IN ('MONTHLY', 'YEARLY')),

    CONSTRAINT chk_payments_status
        CHECK (status IN ('READY', 'SUCCESS', 'FAILED', 'CANCELED', 'RETRYING')),

    CONSTRAINT chk_payments_retry_count
        CHECK (retry_count >= 0 AND retry_count <= 3)
);

-- TossPayments 웹훅 이벤트 수신/중복 처리 이력
CREATE TABLE IF NOT EXISTS payment_webhook_events (
    payment_webhook_event_id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    toss_payment_key VARCHAR(255) NULL,
    payload TEXT NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_payment_webhook_events_event_id
        UNIQUE (event_id)
);

-- 월별 구독 매출 정산 집계
CREATE TABLE IF NOT EXISTS monthly_settlements (
    monthly_settlement_id BIGSERIAL PRIMARY KEY,
    settlement_month VARCHAR(7) NOT NULL,
    gross_amount INTEGER NOT NULL DEFAULT 0,
    canceled_amount INTEGER NOT NULL DEFAULT 0,
    toss_fee_amount INTEGER NOT NULL DEFAULT 0,
    llm_cost_amount INTEGER NOT NULL DEFAULT 0,
    net_amount INTEGER NOT NULL DEFAULT 0,
    subscriber_count INTEGER NOT NULL DEFAULT 0,
    payment_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    confirmed_at TIMESTAMP NULL,
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT uq_monthly_settlements_settlement_month
        UNIQUE (settlement_month),

    CONSTRAINT chk_monthly_settlements_amounts
        CHECK (
            gross_amount >= 0
            AND canceled_amount >= 0
            AND toss_fee_amount >= 0
            AND llm_cost_amount >= 0
            AND net_amount >= 0
        ),

    CONSTRAINT chk_monthly_settlements_counts
        CHECK (subscriber_count >= 0 AND payment_count >= 0),

    CONSTRAINT chk_monthly_settlements_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'PAID', 'CANCELED'))
);

-- 월별 정산에 포함된 결제별 상세 내역
CREATE TABLE IF NOT EXISTS settlement_payment_details (
    settlement_payment_detail_id BIGSERIAL PRIMARY KEY,
    monthly_settlement_id BIGINT NOT NULL,
    payment_id BIGINT NOT NULL,
    amount INTEGER NOT NULL,
    toss_fee_amount INTEGER NOT NULL DEFAULT 0,
    llm_cost_amount INTEGER NOT NULL DEFAULT 0,
    net_amount INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_settlement_payment_details_settlement
        FOREIGN KEY (monthly_settlement_id)
        REFERENCES monthly_settlements(monthly_settlement_id),

    CONSTRAINT fk_settlement_payment_details_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(payment_id),

    CONSTRAINT uq_settlement_payment_details_payment_id
        UNIQUE (payment_id),

    CONSTRAINT chk_settlement_payment_details_amounts
        CHECK (
            amount >= 0
            AND toss_fee_amount >= 0
            AND llm_cost_amount >= 0
            AND net_amount >= 0
        )
);

CREATE INDEX IF NOT EXISTS idx_receipt_analysis_member_id
    ON receipt_analysis(member_id);

CREATE INDEX IF NOT EXISTS idx_receipt_analysis_items_analysis_id
    ON receipt_analysis_items(receipt_analysis_id);

CREATE INDEX IF NOT EXISTS idx_receipt_analysis_items_product_id
    ON receipt_analysis_items(product_id);

CREATE INDEX IF NOT EXISTS idx_billing_keys_member_id
    ON billing_keys(member_id);

CREATE INDEX IF NOT EXISTS idx_payments_member_id
    ON payments(member_id);

CREATE INDEX IF NOT EXISTS idx_payments_billing_key_id
    ON payments(billing_key_id);

CREATE INDEX IF NOT EXISTS idx_payments_approved_at
    ON payments(approved_at);

CREATE INDEX IF NOT EXISTS idx_payment_webhook_events_toss_payment_key
    ON payment_webhook_events(toss_payment_key);

CREATE INDEX IF NOT EXISTS idx_settlement_payment_details_settlement_id
    ON settlement_payment_details(monthly_settlement_id);
