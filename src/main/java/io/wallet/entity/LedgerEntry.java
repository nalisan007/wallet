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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "ledger_entry",
    indexes = {
        @Index(
            name = "idx_ledger_entry_wallet_created_id",
            columnList = "wallet_id, created_at, id"
        ),
        @Index(
            name = "idx_ledger_entry_transaction",
            columnList = "ledger_transaction_id"
        )
    }
)
public class LedgerEntry {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotNull(message = "Ledger transaction ID is required")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "ledger_transaction_id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID ledgerTransactionId;

    @NotNull(message = "Wallet ID is required")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "wallet_id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID walletId;

    @NotNull(message = "Ledger entry type is required")
    @Enumerated(EnumType.STRING)
    @Column(
        name = "entry_type",
        nullable = false,
        length = 10
    )
    private LedgerEntryType entryType;

    @NotNull(message = "Ledger entry amount is required")
    @Positive(message = "Ledger entry amount must be greater than zero")
    @Column(
        name = "amount_paise",
        nullable = false,
        updatable = false
    )
    private Long amountPaise;

    @NotNull(message = "Ledger entry creation time is required")
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    protected LedgerEntry() {
        // Required by JPA.
    }

    public LedgerEntry(
        UUID ledgerTransactionId,
        UUID walletId,
        LedgerEntryType entryType,
        Long amountPaise
    ) {
        this.ledgerTransactionId = ledgerTransactionId;
        this.walletId = walletId;
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

    public UUID getLedgerTransactionId() {
        return ledgerTransactionId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public Long getAmountPaise() {
        return amountPaise;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
