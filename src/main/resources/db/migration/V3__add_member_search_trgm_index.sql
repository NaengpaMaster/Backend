CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_members_nickname_trgm
ON members USING gin (nickname gin_trgm_ops);

CREATE INDEX idx_members_email_trgm
ON members USING gin (email gin_trgm_ops);