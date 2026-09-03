package io.wallet.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    private final UUID walletId;

    public WalletNotFoundException(UUID walletId) {
        super("Wallet not found: " + walletId);
        this.walletId = walletId;
    }

    public WalletNotFoundException(String message) {
        super(message);
        this.walletId = null;
    }

    public UUID getWalletId() {
        return walletId;
    }
}
