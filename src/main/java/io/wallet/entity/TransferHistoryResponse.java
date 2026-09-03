package io.wallet.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TransferHistoryResponse(

    @NotNull(message = "Transfer history is required")
    @Valid
    List<TransferResponse> transfers,

    @NotNull(message = "Next cursor availability is required")
    Boolean hasNext,

    String nextCursor

) {
}
