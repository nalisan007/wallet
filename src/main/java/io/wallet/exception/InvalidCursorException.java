package io.wallet.exception;

public class InvalidCursorException extends RuntimeException {

    private final String cursor;

    public InvalidCursorException(String cursor) {
        super("Invalid pagination cursor");
        this.cursor = cursor;
    }

    public String getCursor() {
        return cursor;
    }
}
