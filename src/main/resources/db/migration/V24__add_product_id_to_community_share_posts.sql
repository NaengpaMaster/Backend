ALTER TABLE community_share_posts
    ADD COLUMN IF NOT EXISTS product_id BIGINT;

ALTER TABLE community_share_posts
    ADD CONSTRAINT fk_community_share_posts_product
        FOREIGN KEY (product_id) REFERENCES products(product_id);

CREATE INDEX IF NOT EXISTS idx_community_share_posts_product_id
    ON community_share_posts(product_id);
