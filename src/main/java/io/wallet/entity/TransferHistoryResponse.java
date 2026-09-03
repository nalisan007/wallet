package io.wallet.entity;

import java.util.List;

public record TransferHistoryResponse(
    List<TransferResponse> transfers,
    String nextCursor
) {
}
