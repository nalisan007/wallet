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
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "wallet",
    indexes = {
        @Index(
            name = "idx_wallet_status",
            columnList = "status"
        ),
        @Index(
            name = "idx_wallet_created_at",
            columnList = "created_at"
        )
    }
)
public class Wallet {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotNull(message = "Wallet balance is required")
    @PositiveOrZero(
        message = "Wallet balance cannot be negative"
    )
    @Column(
        name = "balance_paise",
        nullable = false
    )
    private long balancePaise;

    @NotNull(message = "Wallet status is required")
    @Column(
        name = "status",
        nullable = false,
        length = 20
    )
    private WalletStatus status;

    @NotNull(message = "Wallet creation time is required")
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    @NotNull(message = "Wallet update time is required")
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
        if (balancePaise < 0) {
            throw new IllegalArgumentException(
                "Wallet balance cannot be negative"
            );
        }

        this.balancePaise = balancePaise;
        this.status = status;
    }

    public void debit(long amountPaise) {
        if (amountPaise <= 0) {
            throw new IllegalArgumentException(
                "Debit amount must be greater than zero"
            );
        }

        if (balancePaise < amountPaise) {
            throw new IllegalArgumentException(
                "Wallet balance cannot become negative"
            );
        }

        balancePaise -= amountPaise;
    }

    public void credit(long amountPaise) {
        if (amountPaise <= 0) {
            throw new IllegalArgumentException(
                "Credit amount must be greater than zero"
            );
        }

        try {
            balancePaise = Math.addExact(
                balancePaise,
                amountPaise
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                "Wallet balance exceeds the supported limit",
                exception
            );
        }
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
}
