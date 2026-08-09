ALTER TABLE shopping_items
    ADD COLUMN IF NOT EXISTS fridge_id BIGINT;

UPDATE shopping_items si
SET fridge_id = f.fridge_id
FROM fridges f
WHERE si.member_id = f.owner_member_id
  AND si.fridge_id IS NULL;

ALTER TABLE shopping_items
    ALTER COLUMN fridge_id SET NOT NULL;

ALTER TABLE shopping_items
    ADD CONSTRAINT fk_shopping_items_fridge
        FOREIGN KEY (fridge_id) REFERENCES fridges(fridge_id);

CREATE INDEX IF NOT EXISTS idx_shopping_items_fridge_deleted
    ON shopping_items(fridge_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_shopping_items_fridge_product_active
    ON shopping_items(fridge_id, product_id)
    WHERE is_deleted = FALSE AND is_purchased = FALSE;

CREATE TABLE IF NOT EXISTS shopping_item_histories (
    shopping_item_history_id BIGSERIAL PRIMARY KEY,
    shopping_item_id BIGINT,
    fridge_id BIGINT NOT NULL,
    actor_member_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity VARCHAR(100),
    is_purchased BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_shopping_item_histories_fridge
        FOREIGN KEY (fridge_id) REFERENCES fridges(fridge_id),
    CONSTRAINT fk_shopping_item_histories_actor_member
        FOREIGN KEY (actor_member_id) REFERENCES members(member_id)
);

CREATE INDEX IF NOT EXISTS idx_shopping_item_histories_fridge_created
    ON shopping_item_histories(fridge_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_shopping_item_histories_actor_created
    ON shopping_item_histories(actor_member_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_shopping_item_histories_action_created
    ON shopping_item_histories(action_type, created_at DESC);
