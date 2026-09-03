package io.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "idempotency_records",
    indexes = {
        @Index(
            name = "idx_idempotency_created_at",
            columnList = "created_at"
        ),
        @Index(
            name = "idx_idempotency_transfer",
            columnList = "transfer_id"
        )
    }
)
public class IdempotencyRecord {

    @Id
    @NotNull(
        message = "Idempotency key is required"
    )
    @Column(
        name = "idempotency_key",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID idempotencyKey;

    @NotBlank(
        message = "Request hash is required"
    )
    @Pattern(
        regexp = "^[0-9a-f]{64}$",
        message = "Request hash must be a SHA-256 hexadecimal value"
    )
    @Column(
        name = "request_hash",
        nullable = false,
        length = 64
    )
    private String requestHash;

    @Column(
        name = "transfer_id",
        columnDefinition = "BINARY(16)"
    )
    private UUID transferId;

    @NotNull(
        message = "Idempotency record creation timestamp is required"
    )
    @Column(
        name = "created_at",
        nullable = false
    )
    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(
        UUID idempotencyKey,
        String requestHash
    ) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
