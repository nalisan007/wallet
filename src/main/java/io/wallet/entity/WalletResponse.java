package io.wallet.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

public record WalletResponse(

    @NotNull(message = "Wallet ID is required")
    UUID walletId,

    @NotNull(message = "Wallet type is required")
    WalletType walletType,

    @NotNull(message = "Wallet balance is required")
    @PositiveOrZero(message = "Wallet balance cannot be negative")
    Long balancePaise,

    @NotNull(message = "Wallet status is required")
    WalletStatus status,

    @NotNull(message = "Wallet creation time is required")
    Instant createdAt,

    @NotNull(message = "Wallet update time is required")
    Instant updatedAt

) {
}
