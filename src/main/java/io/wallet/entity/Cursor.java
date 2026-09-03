package io.wallet.entity;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record Cursor(

    @NotNull(message = "Cursor creation time is required")
    Instant createdAt,

    @NotNull(message = "Cursor ID is required")
    UUID id

) {
}
