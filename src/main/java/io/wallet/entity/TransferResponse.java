package io.wallet.entity;

import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
    UUID id,
    UUID fromWalletId,
    UUID toWalletId,
    long amountPaise,
    TransferStatus status,
    Instant createdAt
) {
}
