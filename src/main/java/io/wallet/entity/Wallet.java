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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "wallets",
    indexes = {
        @Index(
            name = "idx_wallets_status",
            columnList = "status"
        )
    }
)
public class Wallet {

    @Id
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @Min(
        value = 0,
        message = "Balance cannot be negative"
    )
    @Column(
        name = "balance_paise",
        nullable = false
    )
    private long balancePaise;

    @NotNull(
        message = "Wallet status is required"
    )
    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 20
    )
    private WalletStatus status;

    @NotNull(
        message = "Created timestamp is required"
    )
    @Column(
        name = "created_at",
        nullable = false
    )
    private Instant createdAt;

    @NotNull(
        message = "Updated timestamp is required"
    )
    @Column(
        name = "updated_at",
        nullable = false
    )
    private Instant updatedAt;

    protected Wallet() {
    }

    public Wallet(
        long balancePaise,
        WalletStatus status
    ) {
        this.balancePaise = balancePaise;
        this.status = status;
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

    public long getBalancePaise() {
        return balancePaise;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void debit(long amountPaise) {
        if (amountPaise <= 0) {
            throw new IllegalArgumentException(
                "Debit amount must be positive"
            );
        }

        if (balancePaise < amountPaise) {
            throw new IllegalArgumentException(
                "Insufficient wallet balance"
            );
        }

        balancePaise -= amountPaise;
    }

    public void credit(long amountPaise) {
        if (amountPaise <= 0) {
            throw new IllegalArgumentException(
                "Credit amount must be positive"
            );
        }

        balancePaise += amountPaise;
    }
}
