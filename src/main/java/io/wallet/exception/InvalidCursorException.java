package io.wallet.exception;

public class InvalidCursorException
    extends RuntimeException {

    private final String message;

    public InvalidCursorException(
        String message
    ) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
