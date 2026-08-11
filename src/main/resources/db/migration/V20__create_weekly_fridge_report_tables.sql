-- =========================================================
-- Weekly fridge consumption report tables
-- =========================================================

CREATE TABLE IF NOT EXISTS consumed_products (
    consumed_product_id BIGSERIAL PRIMARY KEY,
    fridge_id BIGINT NOT NULL,
    actor_member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_category_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    quantity VARCHAR(100) NOT NULL,
    consumed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_consumed_products_fridge
        FOREIGN KEY (fridge_id)
        REFERENCES fridges(fridge_id),
    CONSTRAINT fk_consumed_products_actor_member
        FOREIGN KEY (actor_member_id)
        REFERENCES members(member_id),
    CONSTRAINT fk_consumed_products_product
        FOREIGN KEY (product_id)
        REFERENCES products(product_id),
    CONSTRAINT fk_consumed_products_product_category
        FOREIGN KEY (product_category_id)
        REFERENCES product_categories(product_category_id)
);

CREATE INDEX IF NOT EXISTS idx_consumed_products_fridge_consumed_at
    ON consumed_products(fridge_id, consumed_at);

CREATE INDEX IF NOT EXISTS idx_consumed_products_actor_consumed_at
    ON consumed_products(actor_member_id, consumed_at);

CREATE TABLE IF NOT EXISTS weekly_fridge_report_delivery_logs (
    weekly_fridge_report_delivery_log_id BIGSERIAL PRIMARY KEY,
    fridge_id BIGINT NOT NULL,
    receiver_member_id BIGINT NOT NULL,
    receiver_email VARCHAR(100) NOT NULL,
    report_week VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000) NULL,
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_weekly_report_delivery_fridge
        FOREIGN KEY (fridge_id)
        REFERENCES fridges(fridge_id),
    CONSTRAINT fk_weekly_report_delivery_receiver
        FOREIGN KEY (receiver_member_id)
        REFERENCES members(member_id),
    CONSTRAINT chk_weekly_report_delivery_status
        CHECK (status IN ('SUCCESS', 'FAILED')),
    CONSTRAINT uk_weekly_report_delivery
        UNIQUE (fridge_id, receiver_member_id, report_week)
);

CREATE INDEX IF NOT EXISTS idx_weekly_report_delivery_week_status
    ON weekly_fridge_report_delivery_logs(report_week, status);
