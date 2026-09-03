package io.wallet.entity;

import java.time.Instant;
import java.util.UUID;

public record Cursor(
    Instant createdAt,
    UUID id
) {
}
