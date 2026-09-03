ALTER TABLE idempotency_records
    ADD CONSTRAINT uq_idempotency_transfer
    UNIQUE (transfer_id);
