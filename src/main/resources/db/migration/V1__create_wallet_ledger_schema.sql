-- Wallet and Transfer Ledger
-- V1: Initial wallet, transfer, double-entry ledger and idempotency schema

CREATE TABLE wallet (
    id BINARY(16) NOT NULL,
    wallet_type VARCHAR(20) NOT NULL,
    balance_paise BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,

    CONSTRAINT pk_wallet PRIMARY KEY (id),
    CONSTRAINT chk_wallet_balance_non_negative CHECK (balance_paise >= 0),
    CONSTRAINT chk_wallet_type CHECK (wallet_type IN ('USER', 'SYSTEM')),
    CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'INACTIVE')),

    -- Only one active system wallet is allowed.
    -- NULL values do not conflict in a UNIQUE index, so USER wallets
    -- and inactive SYSTEM wallets can coexist.
    system_wallet_unique_key TINYINT
        GENERATED ALWAYS AS (
            CASE
                WHEN wallet_type = 'SYSTEM' AND status = 'ACTIVE' THEN 1
                ELSE NULL
            END
        ) STORED,

    CONSTRAINT uq_wallet_active_system UNIQUE (system_wallet_unique_key)
);

CREATE INDEX idx_wallet_type_status
    ON wallet (wallet_type, status);


CREATE TABLE transfer (
    id BINARY(16) NOT NULL,
    from_wallet_id BINARY(16) NOT NULL,
    to_wallet_id BINARY(16) NOT NULL,
    amount_paise BIGINT NOT NULL,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,

    CONSTRAINT pk_transfer PRIMARY KEY (id),
    CONSTRAINT fk_transfer_from_wallet
        FOREIGN KEY (from_wallet_id)
        REFERENCES wallet (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_transfer_to_wallet
        FOREIGN KEY (to_wallet_id)
        REFERENCES wallet (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT chk_transfer_amount_positive CHECK (amount_paise > 0),
    CONSTRAINT chk_transfer_no_self_transfer CHECK (from_wallet_id <> to_wallet_id)
);

CREATE INDEX idx_transfer_from_created_id
    ON transfer (from_wallet_id, created_at, id);

CREATE INDEX idx_transfer_to_created_id
    ON transfer (to_wallet_id, created_at, id);


CREATE TABLE ledger_transaction (
    id BINARY(16) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    reference_id BINARY(16) NOT NULL,
    created_at DATETIME(0) NOT NULL,

    CONSTRAINT pk_ledger_transaction PRIMARY KEY (id),
    CONSTRAINT chk_ledger_transaction_type
        CHECK (transaction_type IN ('TRANSFER', 'DEPOSIT', 'WITHDRAWAL'))
);

CREATE INDEX idx_ledger_transaction_type_reference
    ON ledger_transaction (transaction_type, reference_id);


CREATE TABLE ledger_entry (
    id BINARY(16) NOT NULL,
    ledger_transaction_id BINARY(16) NOT NULL,
    wallet_id BINARY(16) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount_paise BIGINT NOT NULL,
    created_at DATETIME(0) NOT NULL,

    CONSTRAINT pk_ledger_entry PRIMARY KEY (id),
    CONSTRAINT fk_ledger_entry_transaction
        FOREIGN KEY (ledger_transaction_id)
        REFERENCES ledger_transaction (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_ledger_entry_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallet (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT chk_ledger_entry_type
        CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_entry_amount_positive CHECK (amount_paise > 0),
    CONSTRAINT uq_ledger_entry_transaction_wallet_type
        UNIQUE (ledger_transaction_id, wallet_id, entry_type)
);

CREATE INDEX idx_ledger_entry_wallet_created_id
    ON ledger_entry (wallet_id, created_at, id);

CREATE INDEX idx_ledger_entry_transaction
    ON ledger_entry (ledger_transaction_id);


CREATE TABLE idempotency_record (
    id BINARY(16) NOT NULL,
    idempotency_key BINARY(16) NOT NULL,
    request_hash BINARY(32) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_status SMALLINT NULL,
    response_body JSON NULL,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,
    completed_at DATETIME(0) NULL,

    CONSTRAINT pk_idempotency_record PRIMARY KEY (id),
    CONSTRAINT uq_idempotency_record_key UNIQUE (idempotency_key),
    CONSTRAINT chk_idempotency_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_idempotency_response_status
        CHECK (
            response_status IS NULL
            OR (response_status BETWEEN 100 AND 599)
        )
);

CREATE INDEX idx_idempotency_status_created
    ON idempotency_record (status, created_at);


-- -------------------------------------------------------------------------
-- Seed data
--
-- UUID literals below are UUIDv7 values represented as 32 hexadecimal
-- characters and converted to BINARY(16).
--
-- The seed balances are established through opening ledger transactions
-- so cached wallet balances and ledger-derived balances agree.
--
-- System opening balance: ₹50,000 = 5,000,000 paise
-- User wallet opening balance: ₹5,000 = 500,000 paise
--
-- These opening entries use the system wallet itself as the accounting
-- account for the initial test fixture. They are seed/bootstrap entries,
-- not a production deposit/withdrawal operation.
-- -------------------------------------------------------------------------

SET @system_wallet_id = UNHEX('01999000000070008000000000000001');
SET @user_wallet_id   = UNHEX('01999000000070008000000000000002');

SET @system_opening_tx_id = UNHEX('01999000000170008000000000000001');
SET @user_opening_tx_id   = UNHEX('01999000000170008000000000000002');

SET @system_opening_entry_id = UNHEX('01999000000270008000000000000001');
SET @user_opening_entry_id   = UNHEX('01999000000270008000000000000002');

INSERT INTO wallet (
    id,
    wallet_type,
    balance_paise,
    status,
    created_at,
    updated_at
)
VALUES
(
    @system_wallet_id,
    'SYSTEM',
    5000000,
    'ACTIVE',
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
),
(
    @user_wallet_id,
    'USER',
    500000,
    'ACTIVE',
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
);


INSERT INTO ledger_transaction (
    id,
    transaction_type,
    reference_id,
    created_at
)
VALUES
(
    @system_opening_tx_id,
    'DEPOSIT',
    @system_wallet_id,
    UTC_TIMESTAMP()
),
(
    @user_opening_tx_id,
    'DEPOSIT',
    @user_wallet_id,
    UTC_TIMESTAMP()
);


INSERT INTO ledger_entry (
    id,
    ledger_transaction_id,
    wallet_id,
    entry_type,
    amount_paise,
    created_at
)
VALUES
(
    @system_opening_entry_id,
    @system_opening_tx_id,
    @system_wallet_id,
    'CREDIT',
    5000000,
    UTC_TIMESTAMP()
),
(
    @user_opening_entry_id,
    @user_opening_tx_id,
    @user_wallet_id,
    'CREDIT',
    500000,
    UTC_TIMESTAMP()
);
