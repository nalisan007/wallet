package io.wallet.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record StatementEntryResponse(

    @NotNull(message = "Ledger entry ID is required")
    UUID entryId,

    @NotNull(message = "Ledger transaction ID is required")
    UUID ledgerTransactionId,

    @NotNull(message = "Transaction type is required")
    LedgerTransactionType transactionType,

    @NotNull(message = "Entry type is required")
    LedgerEntryType entryType,

    @NotNull(message = "Amount is required")
    @Positive(message = "Statement amount must be greater than zero")
    Long amountPaise,

    @NotNull(message = "Entry creation time is required")
    Instant createdAt

) {
}
