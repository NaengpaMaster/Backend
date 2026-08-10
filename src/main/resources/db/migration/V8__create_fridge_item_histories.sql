CREATE TABLE IF NOT EXISTS fridge_item_histories (
    fridge_item_history_id BIGSERIAL PRIMARY KEY,
    fridge_item_id BIGINT,
    fridge_id BIGINT NOT NULL,
    actor_member_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity VARCHAR(100),
    memo VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_fridge_item_histories_fridge
        FOREIGN KEY (fridge_id) REFERENCES fridges(fridge_id),
    CONSTRAINT fk_fridge_item_histories_actor_member
        FOREIGN KEY (actor_member_id) REFERENCES members(member_id)
);

CREATE INDEX IF NOT EXISTS idx_fridge_item_histories_fridge_created
    ON fridge_item_histories(fridge_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_fridge_item_histories_actor_created
    ON fridge_item_histories(actor_member_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_fridge_item_histories_action_created
    ON fridge_item_histories(action_type, created_at DESC);
