CREATE TABLE IF NOT EXISTS fridge_item_share_requests (
    fridge_item_share_request_id BIGSERIAL PRIMARY KEY,
    requester_member_id BIGINT NOT NULL REFERENCES members(member_id),
    requested_member_id BIGINT NOT NULL REFERENCES members(member_id),
    source_fridge_id BIGINT NOT NULL REFERENCES fridges(fridge_id),
    target_fridge_id BIGINT NOT NULL REFERENCES fridges(fridge_id),
    fridge_item_id BIGINT NOT NULL REFERENCES fridge_items(fridge_item_id),
    product_id BIGINT NOT NULL REFERENCES products(product_id),
    requested_quantity VARCHAR(100) NOT NULL,
    message VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    CONSTRAINT fridge_item_share_requests_status_check CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED'))
);

CREATE INDEX idx_fridge_item_share_requests_requested_member_status
    ON fridge_item_share_requests (requested_member_id, status, requested_at DESC);

CREATE INDEX idx_fridge_item_share_requests_requester_status
    ON fridge_item_share_requests (requester_member_id, status, requested_at DESC);
ALTER TABLE notifications
    DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notifications
    ADD CONSTRAINT notifications_type_check
    CHECK (type IN (
        'EXPIRY_SOON',
        'EXPIRED',
        'INQUIRY_ANSWERED',
        'COMMENT_REPLIED',
        'FRIDGE_ITEM_REQUESTED',
        'GENERAL'
    ));

ALTER TABLE notifications
    DROP CONSTRAINT IF EXISTS notifications_target_type_check;

ALTER TABLE notifications
    ADD CONSTRAINT notifications_target_type_check
    CHECK (target_type IS NULL OR target_type IN (
        'FRIDGE_ITEM',
        'FRIDGE_ITEM_SHARE_REQUEST',
        'INQUIRY',
        'COMMENT',
        'RECIPE'
    ));
