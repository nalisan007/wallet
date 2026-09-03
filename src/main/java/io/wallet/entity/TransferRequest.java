package io.wallet.entity;

import io.wallet.config.DifferentWallets;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@DifferentWallets(
    message = "From wallet and to wallet must be different"
)
public record TransferRequest(

    @NotNull(message = "From wallet ID is required")
    UUID fromWalletId,

    @NotNull(message = "To wallet ID is required")
    UUID toWalletId,

    @NotNull(message = "Transfer amount is required")
    @Positive(message = "Amount must be greater than zero")
    Long amountPaise

) {
}
