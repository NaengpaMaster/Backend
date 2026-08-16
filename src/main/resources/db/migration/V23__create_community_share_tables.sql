CREATE TABLE IF NOT EXISTS community_share_posts (
    community_share_post_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    ingredient_name VARCHAR(100) NOT NULL,
    quantity VARCHAR(100) NOT NULL,
    total_price INTEGER NOT NULL,
    participant_limit INTEGER NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    address VARCHAR(255),
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    closed_at TIMESTAMP,
    CONSTRAINT fk_community_share_posts_member
        FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT chk_community_share_posts_status
        CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED')),
    CONSTRAINT chk_community_share_posts_total_price
        CHECK (total_price >= 0),
    CONSTRAINT chk_community_share_posts_participant_limit
        CHECK (participant_limit BETWEEN 2 AND 20)
);

CREATE TABLE IF NOT EXISTS community_share_participants (
    community_share_participant_id BIGSERIAL PRIMARY KEY,
    community_share_post_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_community_share_participants_post
        FOREIGN KEY (community_share_post_id)
        REFERENCES community_share_posts(community_share_post_id),
    CONSTRAINT fk_community_share_participants_member
        FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT uk_community_share_participants_post_member
        UNIQUE (community_share_post_id, member_id),
    CONSTRAINT chk_community_share_participants_status
        CHECK (status IN ('JOINED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_community_share_posts_status_created
    ON community_share_posts(status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_community_share_posts_member_created
    ON community_share_posts(member_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_community_share_participants_member_created
    ON community_share_participants(member_id, created_at DESC);
