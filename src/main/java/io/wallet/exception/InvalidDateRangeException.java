package io.wallet.exception;

import java.time.Instant;

public class InvalidDateRangeException
    extends RuntimeException {

    private final String message;
    private final Instant from;
    private final Instant to;

    public InvalidDateRangeException(
        Instant from,
        Instant to
    ) {
        this(
            "Invalid date range: 'to' must be greater than "
                + "or equal to 'from'",
            from,
            to
        );
    }

    public InvalidDateRangeException(
        String message,
        Instant from,
        Instant to
    ) {
        super(message);
        this.message = message;
        this.from = from;
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }
}
