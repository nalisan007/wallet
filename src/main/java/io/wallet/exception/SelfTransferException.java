package io.wallet.exception;

import java.util.UUID;

public class SelfTransferException extends RuntimeException {

    private final String message;
    private final UUID walletId;

    public SelfTransferException(UUID walletId) {
        this.message =
            "Source and destination wallets must be different";
        this.walletId = walletId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public UUID getWalletId() {
        return walletId;
    }
}
