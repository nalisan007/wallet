-- Development seed data for local MVP testing.
-- These fixed UUIDs are intentionally stable so the React frontend can be tested directly.
INSERT INTO wallets (
    id,
    balance_paise,
    status,
    created_at,
    updated_at
)
VALUES (
    UNHEX('01999000000070008000000000000001'),
    5000000,
    'ACTIVE',
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
),
(
    UNHEX('01999000000070008000000000000002'),
    500000,
    'ACTIVE',
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
);
-- Seed user associated with USER wallet
INSERT INTO wallet_user (
    wallet_id,
    user_id
)
VALUES (
   UNHEX(REPLACE('01999000-0000-7000-8000-000000000001', '-', '')),
    '1'
);
