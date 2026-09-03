package io.wallet.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record WalletActivityResponse(

    @NotNull(message = "Transfer ID is required")
    UUID transferId,

    @NotNull(message = "Transaction type is required")
    LedgerTransactionType transactionType,

    @NotNull(message = "Entry type is required")
    LedgerEntryType entryType,

    @NotNull(message = "Activity amount is required")
    @Positive(message = "Activity amount must be greater than zero")
    Long amountPaise,

    @NotNull(message = "Activity creation time is required")
    Instant createdAt

) {
}
