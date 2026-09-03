package io.wallet.exception;

import java.util.UUID;

public class IdempotencyKeyProcessingException extends RuntimeException {

    private final UUID idempotencyKey;

    public IdempotencyKeyProcessingException(UUID idempotencyKey) {
        super("A request with this idempotency key is already being processed");
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
