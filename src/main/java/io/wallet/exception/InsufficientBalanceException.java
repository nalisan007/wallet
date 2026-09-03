package io.wallet.exception;

import java.util.UUID;

public class InsufficientBalanceException
    extends RuntimeException {

    private final String message;
    private final UUID walletId;
    private final long requestedAmountPaise;
    private final long availableBalancePaise;

    public InsufficientBalanceException(
        UUID walletId,
        long requestedAmountPaise,
        long availableBalancePaise
    ) {
        this.message =
            "Insufficient balance for wallet: " + walletId;

        this.walletId = walletId;
        this.requestedAmountPaise = requestedAmountPaise;
        this.availableBalancePaise = availableBalancePaise;
    }

    @Override
    public String getMessage() {
        return message;
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
