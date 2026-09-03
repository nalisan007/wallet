package io.wallet.exception;

import java.util.UUID;

public class WalletInactiveException extends RuntimeException {

    private final String message;
    private final UUID walletId;

    public WalletInactiveException(UUID walletId) {
        super("Wallet is inactive: " + walletId);
        this.message =
            "Wallet is inactive: " + walletId;
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
