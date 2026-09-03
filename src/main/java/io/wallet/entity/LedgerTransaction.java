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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "ledger_transactions",
    indexes = {
        @Index(
            name = "idx_ledger_wallet_created",
            columnList = "wallet_id, created_at, id"
        ),
        @Index(
            name = "idx_ledger_transfer",
            columnList = "transfer_id"
        )
    }
)
public class LedgerTransaction {

    @Id
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotNull(
        message = "Transfer ID is required"
    )
    @Column(
        name = "transfer_id",
        nullable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID transferId;

    @NotNull(
        message = "Wallet ID is required"
    )
    @Column(
        name = "wallet_id",
        nullable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID walletId;

    @NotNull(
        message = "Ledger entry type is required"
    )
    @Enumerated(EnumType.STRING)
    @Column(
        name = "entry_type",
        nullable = false,
        length = 10
    )
    private LedgerEntryType entryType;

    @Positive(
        message = "Ledger amount must be positive"
    )
    @Column(
        name = "amount_paise",
        nullable = false
    )
    private long amountPaise;

    @NotNull(
        message = "Ledger creation timestamp is required"
    )
    @Column(
        name = "created_at",
        nullable = false
    )
    private Instant createdAt;

    protected LedgerTransaction() {
    }

    public LedgerTransaction(
        UUID walletId,
        UUID transferId,
        LedgerEntryType entryType,
        long amountPaise
    ) {
        this.walletId = walletId;
        this.transferId = transferId;
        this.entryType = entryType;
        this.amountPaise = amountPaise;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
