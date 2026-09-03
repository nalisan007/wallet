CREATE TABLE wallets (
    id BINARY(16) NOT NULL,
    balance_paise BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT pk_wallets
        PRIMARY KEY (id),

    CONSTRAINT chk_wallets_balance_non_negative
        CHECK (balance_paise >= 0),

    CONSTRAINT chk_wallets_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

CREATE INDEX idx_wallets_status
    ON wallets (status);

CREATE TABLE transfers (
    id BINARY(16) NOT NULL,
    from_wallet_id BINARY(16) NOT NULL,
    to_wallet_id BINARY(16) NOT NULL,
    amount_paise BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6) NULL,

    CONSTRAINT pk_transfers
        PRIMARY KEY (id),

    CONSTRAINT fk_transfers_from_wallet
        FOREIGN KEY (from_wallet_id)
        REFERENCES wallets (id),

    CONSTRAINT fk_transfers_to_wallet
        FOREIGN KEY (to_wallet_id)
        REFERENCES wallets (id),

    CONSTRAINT chk_transfers_amount_positive
        CHECK (amount_paise > 0),

    CONSTRAINT chk_transfers_different_wallets
        CHECK (from_wallet_id <> to_wallet_id),

    CONSTRAINT chk_transfers_status
        CHECK (status IN ('COMPLETED', 'FAILED'))
);

CREATE INDEX idx_transfers_from_wallet_created
    ON transfers (from_wallet_id, created_at DESC, id DESC);

CREATE INDEX idx_transfers_to_wallet_created
    ON transfers (to_wallet_id, created_at DESC, id DESC);

CREATE INDEX idx_transfers_created
    ON transfers (created_at DESC, id DESC);

CREATE TABLE ledger_transactions (
    id BINARY(16) NOT NULL,
    transfer_id BINARY(16) NOT NULL,
    wallet_id BINARY(16) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount_paise BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT pk_ledger_transactions
        PRIMARY KEY (id),

    CONSTRAINT fk_ledger_transfer
        FOREIGN KEY (transfer_id)
        REFERENCES transfers (id),

    CONSTRAINT fk_ledger_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets (id),

    CONSTRAINT chk_ledger_amount_positive
        CHECK (amount_paise > 0),

    CONSTRAINT chk_ledger_entry_type
        CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);

CREATE INDEX idx_ledger_wallet_created
    ON ledger_transactions (wallet_id, created_at ASC, id ASC
