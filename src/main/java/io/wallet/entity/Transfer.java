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
            name = "idx_transfer_from_created_id",
            columnList = "from_wallet_id, created_at, id"
        ),
        @Index(
            name = "idx_transfer_to_created_id",
            columnList = "to_wallet_id, created_at, id"
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

    @NotNull
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "from_wallet_id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID fromWalletId;

    @NotNull
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "to_wallet_id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID toWalletId;

    @NotNull
    @Positive
    @Column(
        name = "amount_paise",
        nullable = false,
        updatable = false
    )
    private Long amountPaise;

    @NotNull
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    @NotNull
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
        Long amountPaise
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

    public Long getAmountPaise() {
        return amountPaise;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
