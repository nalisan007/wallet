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
            name = "idx_wallet_type_status",
            columnList = "wallet_type, status"
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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = "wallet_type",
        nullable = false,
        length = 20
    )
    private WalletType walletType;

    @NotNull
    @PositiveOrZero
    @Column(
        name = "balance_paise",
        nullable = false
    )
    private Long balancePaise;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 20
    )
    private WalletStatus status;

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

    protected Wallet() {

    }

    public Wallet(
        WalletType walletType,
        Long balancePaise,
        WalletStatus status
    ) {
        this.walletType = walletType;
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

    public WalletType getWalletType() {
        return walletType;
    }

    public Long getBalancePaise() {
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
