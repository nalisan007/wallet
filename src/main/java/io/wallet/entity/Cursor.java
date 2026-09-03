package io.wallet.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record Cursor(
    Instant createdAt,
    UUID id
) {

    @JsonCreator
    public Cursor(
        @JsonProperty("createdAt")
        Instant createdAt,

        @JsonProperty("id")
        UUID id
    ) {
        this.createdAt = createdAt;
        this.id = id;
    }
}
