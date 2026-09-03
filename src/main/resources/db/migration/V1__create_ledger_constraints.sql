ALTER TABLE ledger_transaction
    ADD CONSTRAINT chk_ledger_transaction_amount_positive
    CHECK (amount_paise > 0);

ALTER TABLE ledger_transaction
    ADD CONSTRAINT chk_ledger_transaction_entry_type
    CHECK (entry_type IN ('DEBIT', 'CREDIT'));

ALTER TABLE transfer
    ADD CONSTRAINT chk_transfer_amount_positive
    CHECK (amount_paise > 0);

ALTER TABLE transfer
    ADD CONSTRAINT chk_transfer_different_wallets
    CHECK (from_wallet_id <> to_wallet_id);

ALTER TABLE wallet
    ADD CONSTRAINT chk_wallet_balance_non_negative
    CHECK (balance_paise >= 0);
