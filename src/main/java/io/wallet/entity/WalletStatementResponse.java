package io.wallet.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WalletStatementResponse(

    @NotNull(message = "Wallet ID is required")
    UUID walletId,

    @NotNull(message = "Statement start time is required")
    Instant from,

    @NotNull(message = "Statement end time is required")
    Instant to,

    @NotNull(message = "Opening balance is required")
    @PositiveOrZero(message = "Opening balance cannot be negative")
    Long openingBalancePaise,

    @NotNull(message = "Statement entries are required")
    @Valid
    List<StatementEntryResponse> entries,

    @NotNull(message = "Closing balance is required")
    @PositiveOrZero(message = "Closing balance cannot be negative")
    Long closingBalancePaise

) {
}
