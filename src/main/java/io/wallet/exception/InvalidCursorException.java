package io.wallet.exception;

public class InvalidCursorException extends RuntimeException {

    private final String message;

    public InvalidCursorException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
