package io.wallet.config;

import io.wallet.exception.InvalidIdempotencyKeyException;

import java.util.UUID;

public final class IdempotencyKeyValidator {

    private IdempotencyKeyValidator() {
    }

    public static UUID parse(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key header is required"
            );
        }

        final UUID uuid;

        try {
            uuid = UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key must be a valid UUIDv7"
            );
        }

        if (uuid.version() != 7) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key must be a UUIDv7"
            );
        }

        return uuid;
    }
}
