package io.wallet.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "transfers",
    indexes = {
        @Index(
            name = "idx_transfers_from_wallet_created",
            columnList = "from_wallet_id, created_at, id"
        ),
        @Index(
            name = "idx_transfers_to_wallet_created",
            columnList = "to_wallet_id, created_at, id"
        ),
        @Index(
            name = "idx_transfers_created",
            columnList = "created_at, id"
        )
    }
)
public class Transfer {

    @Id
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotNull(
        message = "Source wallet ID is required"
    )
    @Column(
        name = "from_wallet_id",
        nullable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID fromWalletId;

    @NotNull(
        message = "Destination wallet ID is required"
    )
    @Column(
        name = "to_wallet_id",
        nullable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID toWalletId;

    @Positive(
        message = "Transfer amount must be positive"
    )
    @Column(
        name = "amount_paise",
        nullable = false
    )
    private long amountPaise;

    @NotNull(
        message = "Transfer status is required"
    )
    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 20
    )
    private TransferStatus status;

    @NotNull(
        message = "Transfer creation timestamp is required"
    )
    @Column(
        name = "created_at",
        nullable = false
    )
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

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
        this.status = TransferStatus.COMPLETED;
    }

    @AssertTrue(
        message = "Source and destination wallets must be different"
    )
    public boolean hasDifferentWallets() {
        return fromWalletId != null
            && toWalletId != null
            && !fromWalletId.equals(toWalletId);
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (status == null) {
            status = TransferStatus.COMPLETED;
        }

        if (status == TransferStatus.COMPLETED
            && completedAt == null) {
            completedAt = Instant.now();
        }
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

    public TransferStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
