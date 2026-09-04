CREATE TABLE wallet_user (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_wallet_user_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_wallet_user_status ON wallet_user (status);

CREATE TABLE wallets (
    id BINARY(16) NOT NULL,
    balance_paise BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_wallet_balance_non_negative
        CHECK (balance_paise >= 0),
    CONSTRAINT chk_wallet_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
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
    PRIMARY KEY (id),
    CONSTRAINT fk_transfers_from_wallet
        FOREIGN KEY (from_wallet_id) REFERENCES wallets(id),
    CONSTRAINT fk_transfers_to_wallet
        FOREIGN KEY (to_wallet_id) REFERENCES wallets(id),
    CONSTRAINT chk_transfer_amount_positive
        CHECK (amount_paise > 0),
    CONSTRAINT chk_transfer_different_wallets
        CHECK (from_wallet_id <> to_wallet_id),
    CONSTRAINT chk_transfer_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_transfers_from_wallet_created
    ON transfers (from_wallet_id, created_at, id);
CREATE INDEX idx_transfers_to_wallet_created
    ON transfers (to_wallet_id, created_at, id);
CREATE INDEX idx_transfers_created
    ON transfers (created_at, id);

CREATE TABLE ledger_transactions (
    id BINARY(16) NOT NULL,
    transfer_id BINARY(16) NOT NULL,
    wallet_id BINARY(16) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount_paise BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ledger_transactions_transfer
        FOREIGN KEY (transfer_id) REFERENCES transfers(id),
    CONSTRAINT fk_ledger_transactions_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT chk_ledger_transaction_amount_positive
        CHECK (amount_paise > 0),
    CONSTRAINT chk_ledger_transaction_entry_type
        CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT uq_ledger_transaction_transfer_type
        UNIQUE (transfer_id, entry_type)
);

CREATE INDEX idx_ledger_wallet_created
    ON ledger_transactions (wallet_id, created_at, id);
CREATE INDEX idx_ledger_transfer
    ON ledger_transactions (transfer_id);

CREATE TABLE idempotency_records (
    idempotency_key BINARY(16) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    transfer_id BINARY(16) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (idempotency_key),
    CONSTRAINT fk_idempotency_transfer
        FOREIGN KEY (transfer_id) REFERENCES transfers(id),
    CONSTRAINT uq_idempotency_transfer
        UNIQUE (transfer_id)
);

CREATE INDEX idx_idempotency_created_at
    ON idempotency_records (created_at);
CREATE INDEX idx_idempotency_transfer
    ON idempotency_records (transfer_id);
