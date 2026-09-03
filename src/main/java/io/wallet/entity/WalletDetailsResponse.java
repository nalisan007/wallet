package io.wallet.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record WalletDetailsResponse(

    @NotNull(message = "Wallet ID is required")
    UUID walletId,

    @NotNull(message = "Wallet type is required")
    WalletType walletType,

    @NotNull(message = "Wallet balance is required")
    Long balancePaise,

    @NotNull(message = "Wallet status is required")
    WalletStatus status,

    @NotNull(message = "Recent activity is required")
    @Valid
    List<WalletActivityResponse> recentActivity

) {
}
