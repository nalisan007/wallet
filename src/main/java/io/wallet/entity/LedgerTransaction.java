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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "ledger_transaction",
    indexes = {
        @Index(
            name = "idx_ledger_transaction_type_reference",
            columnList = "transaction_type, reference_id"
        )
    }
)
public class LedgerTransaction {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotNull(message = "Ledger transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(
        name = "transaction_type",
        nullable = false,
        length = 20
    )
    private LedgerTransactionType transactionType;

    @NotNull(message = "Ledger transaction reference ID is required")
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "reference_id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID referenceId;

    @NotNull(message = "Ledger transaction creation time is required")
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    protected LedgerTransaction() {
        // Required by JPA.
    }

    public LedgerTransaction(
        LedgerTransactionType transactionType,
        UUID referenceId
    ) {
        this.transactionType = transactionType;
        this.referenceId = referenceId;
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

    public LedgerTransactionType getTransactionType() {
        return transactionType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
