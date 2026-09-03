package io.wallet.exception;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {

    private final UUID walletId;
    private final long requestedAmountPaise;
    private final long availableBalancePaise;

    public InsufficientBalanceException(
        UUID walletId,
        long requestedAmountPaise,
        long availableBalancePaise
    ) {
        super("Insufficient wallet balance");
        this.walletId = walletId;
        this.requestedAmountPaise = requestedAmountPaise;
        this.availableBalancePaise = availableBalancePaise;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public long getRequestedAmountPaise() {
        return requestedAmountPaise;
    }

    public long getAvailableBalancePaise() {
        return availableBalancePaise;
    }
}
