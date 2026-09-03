package io.wallet.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "transfer",
    indexes = {
        @Index(
            name = "idx_transfer_from_wallet_created_at",
            columnList = "from_wallet_id, created_at, id"
        ),
        @Index(
            name = "idx_transfer_to_wallet_created_at",
            columnList = "to_wallet_id, created_at, id"
        ),
        @Index(
            name = "idx_transfer_created_at",
            columnList = "created_at"
        )
    }
)
public class Transfer {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotNull(message = "From wallet ID is required")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "from_wallet_id",
        nullable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID fromWalletId;

    @NotNull(message = "To wallet ID is required")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "to_wallet_id",
        nullable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID toWalletId;

    @Positive(message = "Transfer amount must be greater than zero")
    @Column(
        name = "amount_paise",
        nullable = false
    )
    private long amountPaise;

    @NotNull(message = "Transfer creation time is required")
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    @NotNull(message = "Transfer update time is required")
    @Column(
        name = "updated_at",
        nullable = false
    )
    private Instant updatedAt;

    protected Transfer() {
    }

    public Transfer(
        UUID fromWalletId,
        UUID toWalletId,
        long amountPaise
    ) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amountPaise = amountPaise;
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

    public UUID getFromWalletId() {
        return fromWalletId;
    }

    public UUID getToWalletId() {
        return toWalletId;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
