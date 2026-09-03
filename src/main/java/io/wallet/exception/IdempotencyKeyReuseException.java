package io.wallet.exception;

import java.util.UUID;

public class IdempotencyKeyReuseException extends RuntimeException {

    private final UUID idempotencyKey;

    public IdempotencyKeyReuseException(UUID idempotencyKey) {
        super("Idempotency key was already used with a different request");
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
