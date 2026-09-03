package io.wallet.entity;

import java.util.List;

public record TransferHistoryResponse(
    List<TransferResponse> transfers,
    String nextCursor,
    boolean hasNext
) {
    public TransferHistoryResponse {
        transfers =
            transfers == null
                ? List.of()
                : List.copyOf(transfers);
    }
}
