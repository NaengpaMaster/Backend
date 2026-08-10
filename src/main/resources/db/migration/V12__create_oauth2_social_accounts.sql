CREATE TABLE IF NOT EXISTS social_accounts (
    social_account_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_social_accounts_member
        FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT uk_social_accounts_provider_user
        UNIQUE (provider, provider_user_id)
);

CREATE TABLE IF NOT EXISTS oauth2_signup_tokens (
    oauth2_signup_token_id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    token VARCHAR(100) NOT NULL UNIQUE,
    expired_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_social_accounts_member
    ON social_accounts(member_id);

CREATE INDEX IF NOT EXISTS idx_oauth2_signup_tokens_token
    ON oauth2_signup_tokens(token);

CREATE INDEX IF NOT EXISTS idx_oauth2_signup_tokens_provider_user
    ON oauth2_signup_tokens(provider, provider_user_id);
