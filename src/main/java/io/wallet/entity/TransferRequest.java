package io.wallet.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record TransferRequest(

    @NotNull(
        message = "Source wallet ID is required"
    )
    UUID fromWalletId,

    @NotNull(
        message = "Destination wallet ID is required"
    )
    UUID toWalletId,

    @NotNull(
        message = "Transfer amount is required"
    )
    @Positive(
        message = "Transfer amount must be positive"
    )
    Long amountPaise
) {
}
