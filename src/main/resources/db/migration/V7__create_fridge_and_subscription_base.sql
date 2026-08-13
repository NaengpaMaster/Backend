CREATE TABLE IF NOT EXISTS fridges (
    fridge_id BIGSERIAL PRIMARY KEY,
    owner_member_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_fridges_owner_member
        FOREIGN KEY (owner_member_id) REFERENCES members(member_id)
);

CREATE TABLE IF NOT EXISTS fridge_members (
    fridge_member_id BIGSERIAL PRIMARY KEY,
    fridge_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    left_at TIMESTAMP,
    CONSTRAINT fk_fridge_members_fridge
        FOREIGN KEY (fridge_id) REFERENCES fridges(fridge_id),
    CONSTRAINT fk_fridge_members_member
        FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT uk_fridge_members_fridge_member
        UNIQUE (fridge_id, member_id)
);

CREATE TABLE IF NOT EXISTS fridge_invites (
    fridge_invite_id BIGSERIAL PRIMARY KEY,
    fridge_id BIGINT NOT NULL,
    inviter_member_id BIGINT NOT NULL,
    invitee_member_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    responded_at TIMESTAMP,
    CONSTRAINT fk_fridge_invites_fridge
        FOREIGN KEY (fridge_id) REFERENCES fridges(fridge_id),
    CONSTRAINT fk_fridge_invites_inviter
        FOREIGN KEY (inviter_member_id) REFERENCES members(member_id),
    CONSTRAINT fk_fridge_invites_invitee
        FOREIGN KEY (invitee_member_id) REFERENCES members(member_id)
);

CREATE TABLE IF NOT EXISTS subscription_plans (
    subscription_plan_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    price INTEGER NOT NULL,
    billing_period VARCHAR(20) NOT NULL,
    billing_interval INTEGER NOT NULL DEFAULT 1,
    trial_days INTEGER NOT NULL DEFAULT 0,
    family_share_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

ALTER TABLE subscription_plans
    DROP CONSTRAINT IF EXISTS subscription_plans_billing_period_check;

ALTER TABLE subscription_plans
    ADD CONSTRAINT subscription_plans_billing_period_check
        CHECK (billing_period IN ('MONTH', 'YEAR'));

CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    fridge_id BIGINT NOT NULL,
    subscription_plan_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    trial_started_at TIMESTAMP,
    trial_ends_at TIMESTAMP,
    current_period_start_at TIMESTAMP,
    current_period_end_at TIMESTAMP,
    next_billing_at TIMESTAMP,
    canceled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_subscriptions_member
        FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT fk_subscriptions_fridge
        FOREIGN KEY (fridge_id) REFERENCES fridges(fridge_id),
    CONSTRAINT fk_subscriptions_plan
        FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plans(subscription_plan_id)
);

CREATE INDEX IF NOT EXISTS idx_fridges_owner_member_id
    ON fridges(owner_member_id);

CREATE INDEX IF NOT EXISTS idx_fridge_members_member_id
    ON fridge_members(member_id);

CREATE INDEX IF NOT EXISTS idx_fridge_members_fridge_status
    ON fridge_members(fridge_id, status);

CREATE INDEX IF NOT EXISTS idx_fridge_invites_invitee_status
    ON fridge_invites(invitee_member_id, status);

CREATE INDEX IF NOT EXISTS idx_fridge_invites_fridge_status
    ON fridge_invites(fridge_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_fridge_invites_pending
    ON fridge_invites(fridge_id, invitee_member_id)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_subscriptions_member_id
    ON subscriptions(member_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_fridge_id
    ON subscriptions(fridge_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_status
    ON subscriptions(status);

CREATE INDEX IF NOT EXISTS idx_subscriptions_next_billing_at
    ON subscriptions(next_billing_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_subscriptions_active_fridge
    ON subscriptions(fridge_id)
    WHERE status IN ('TRIALING', 'ACTIVE', 'CANCELED');

INSERT INTO subscription_plans (
    code,
    name,
    price,
    billing_period,
    billing_interval,
    trial_days,
    family_share_enabled,
    active,
    created_at
) VALUES (
    'MONTHLY_PREMIUM',
    '월간 프리미엄',
    2900,
    'MONTH',
    1,
    7,
    TRUE,
    TRUE,
    NOW()
), (
    'YEARLY_PREMIUM',
    '연간 프리미엄',
    27840,
    'YEAR',
    1,
    7,
    TRUE,
    TRUE,
    NOW()
)
ON CONFLICT (code) DO NOTHING;

INSERT INTO fridges (
    owner_member_id,
    name,
    status,
    created_at
)
SELECT
    member_id,
    COALESCE(NULLIF(TRIM(nickname), ''), '기본') || '의 냉장고',
    'ACTIVE',
    NOW()
FROM members
WHERE NOT EXISTS (
    SELECT 1
    FROM fridges f
    WHERE f.owner_member_id = members.member_id
);

INSERT INTO fridge_members (
    fridge_id,
    member_id,
    role,
    status,
    joined_at
)
SELECT
    fridge_id,
    owner_member_id,
    'OWNER',
    'ACTIVE',
    NOW()
FROM fridges
WHERE NOT EXISTS (
    SELECT 1
    FROM fridge_members fm
    WHERE fm.fridge_id = fridges.fridge_id
      AND fm.member_id = fridges.owner_member_id
);

ALTER TABLE fridge_items
    ADD COLUMN IF NOT EXISTS fridge_id BIGINT;

UPDATE fridge_items fi
SET fridge_id = f.fridge_id
FROM fridges f
WHERE fi.member_id = f.owner_member_id
  AND fi.fridge_id IS NULL;

ALTER TABLE fridge_items
    ALTER COLUMN fridge_id SET NOT NULL;

ALTER TABLE fridge_items
    ADD CONSTRAINT fk_fridge_items_fridge
        FOREIGN KEY (fridge_id) REFERENCES fridges(fridge_id);

CREATE INDEX IF NOT EXISTS idx_fridge_items_fridge_id
    ON fridge_items(fridge_id);
