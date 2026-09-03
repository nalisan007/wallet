package io.wallet.exception;

public class InvalidIdempotencyKeyException
    extends RuntimeException {

    private final String message;

    public InvalidIdempotencyKeyException(
        String message
    ) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
