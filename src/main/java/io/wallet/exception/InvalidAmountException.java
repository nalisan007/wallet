package io.wallet.exception;

public class InvalidAmountException extends RuntimeException {

    private final long amountPaise;

    public InvalidAmountException(long amountPaise) {
        super("Amount must be greater than zero");
        this.amountPaise = amountPaise;
    }

    public long getAmountPaise() {
        return amountPaise;
    }
}
