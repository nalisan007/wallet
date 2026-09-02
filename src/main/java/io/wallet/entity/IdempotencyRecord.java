package io.wallet.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "idempotency_record",
    indexes = {
        @Index(
            name = "idx_idempotency_status_created",
            columnList = "status, created_at"
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
        updatable = false,
        unique = true,
        columnDefinition = "BINARY(16)"
    )
    private UUID idempotencyKey;

    @NotNull(message = "Request hash is required")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "request_hash",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(32)"
    )
    private byte[] requestHash;

    @NotNull(message = "Idempotency status is required")
    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 20
    )
    private IdempotencyStatus status;

    @Min(
        value = 100,
        message = "Response status must be at least 100"
    )
    @Max(
        value = 599,
        message = "Response status must not exceed 599"
    )
    @Column(name = "response_status")
    private Integer responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "response_body",
        columnDefinition = "JSON"
    )
    private String responseBody;

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

    @Column(name = "completed_at")
    private Instant completedAt;

    protected IdempotencyRecord() {
        // Required by JPA.
    }

    public IdempotencyRecord(
        UUID idempotencyKey,
        byte[] requestHash
    ) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = IdempotencyStatus.PROCESSING;
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

    public void markCompleted(
        Integer responseStatus,
        String responseBody
    ) {
        this.status = IdempotencyStatus.COMPLETED;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.completedAt = Instant.now();
    }

    public void markFailed(
        Integer responseStatus,
        String responseBody
    ) {
        this.status = IdempotencyStatus.FAILED;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.completedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public byte[] getRequestHash() {
        return requestHash;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
