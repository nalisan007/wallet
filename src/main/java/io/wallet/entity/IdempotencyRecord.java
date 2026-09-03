package io.wallet.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "idempotency_record",
    indexes = {
        @Index(
            name = "idx_idempotency_record_key",
            columnList = "idempotency_key"
        ),
        @Index(
            name = "idx_idempotency_record_created_at",
            columnList = "created_at"
        )
    }
)
public class IdempotencyRecord {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotNull(message = "Idempotency key is required")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "idempotency_key",
        nullable = false,
        unique = true,
        columnDefinition = "BINARY(16)"
    )
    private UUID idempotencyKey;

    @NotBlank(message = "Request hash is required")
    @Size(
        min = 64,
        max = 64,
        message = "Request hash must contain exactly 64 characters"
    )
    @Column(
        name = "request_hash",
        nullable = false,
        length = 64
    )
    private String requestHash;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "transfer_id",
        columnDefinition = "BINARY(16)"
    )
    private UUID transferId;

    @NotNull(message = "Idempotency record creation time is required")
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    @NotNull(message = "Idempotency record update time is required")
    @Column(
        name = "updated_at",
        nullable = false
    )
    private Instant updatedAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(
        UUID idempotencyKey,
        String requestHash
    ) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }
}
