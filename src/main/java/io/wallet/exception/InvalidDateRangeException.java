package io.wallet.exception;

import java.time.Instant;

public class InvalidDateRangeException extends RuntimeException {

    private final Instant from;
    private final Instant to;

    public InvalidDateRangeException(
        Instant from,
        Instant to
    ) {
        super("To date must be greater than or equal to from date");
        this.from = from;
        this.to = to;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }
}
