package io.wallet.entity;

import java.time.Instant;
import java.util.UUID;

public record StatementEntry(
    UUID transactionId,
    UUID transferId,
    LedgerEntryType entryType,
    long amountPaise,
    long balanceAfterPaise,
    Instant createdAt
) {
}
