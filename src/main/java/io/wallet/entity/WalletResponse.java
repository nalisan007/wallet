package io.wallet.entity;

import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
    UUID id,
    long balancePaise,
    String status,
    Instant createdAt
) {
}
