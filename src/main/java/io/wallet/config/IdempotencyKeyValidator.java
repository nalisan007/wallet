package io.wallet.config;

import com.github.f4b6a3.uuid.UuidCreator;
import io.wallet.exception.InvalidIdempotencyKeyException;

import java.util.UUID;

public final class IdempotencyKeyValidator {

    private IdempotencyKeyValidator() {
    }

    public static UUID parse(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key header is required"
            );
        }

        final UUID parsedKey;

        try {
            parsedKey = UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key must be a valid UUIDv7"
            );
        }

        if (parsedKey.version() != 7) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key must be a UUIDv7"
            );
        }

        return parsedKey;
    }
}
