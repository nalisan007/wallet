package io.wallet.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    private final String message;
    private final UUID walletId;

    public WalletNotFoundException(UUID walletId) {
        this.message =
            "Wallet not found: " + walletId;
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
