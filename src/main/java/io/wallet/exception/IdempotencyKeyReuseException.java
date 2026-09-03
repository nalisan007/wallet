package io.wallet.exception;

import java.util.UUID;

public class IdempotencyKeyReuseException extends RuntimeException {

    private final String message;
    private final UUID idempotencyKey;

    public IdempotencyKeyReuseException(UUID idempotencyKey) {
        this.message =
            "Idempotency key was already used with a different request";
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
