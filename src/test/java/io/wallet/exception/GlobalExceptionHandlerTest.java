package io.wallet.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
        new GlobalExceptionHandler();

    @Test
    void shouldMapWalletNotFoundTo404() {
        UUID walletId = UUID.randomUUID();

        ResponseEntity<GlobalExceptionHandler.ApiError> response =
            handler.handleWalletNotFound(
                new WalletNotFoundException(walletId)
            );

        assertEquals(
            HttpStatus.NOT_FOUND,
            response.getStatusCode()
        );

        assertEquals(
            "WALLET_NOT_FOUND",
            response.getBody().code()
        );
    }

    @Test
    void shouldMapInvalidCursorTo400() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response =
            handler.handleInvalidCursor(
                new InvalidCursorException(
                    "Invalid cursor"
                )
            );

        assertEquals(
            HttpStatus.BAD_REQUEST,
            response.getStatusCode()
        );

        assertEquals(
            "INVALID_CURSOR",
            response.getBody().code()
        );
    }

    @Test
    void shouldMapIdempotencyProcessingTo409() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response =
            handler.handleIdempotencyKeyProcessing(
                new IdempotencyKeyProcessingException(UUID.randomUUID())
            );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("IDEMPOTENCY_KEY_PROCESSING", response.getBody().code());
    }

    @Test
    void shouldMapIdempotencyReuseTo409() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response =
            handler.handleIdempotencyKeyReuse(
                new IdempotencyKeyReuseException(UUID.randomUUID())
            );

        assertEquals(
            HttpStatus.CONFLICT,
            response.getStatusCode()
        );

        assertEquals(
            "IDEMPOTENCY_KEY_REUSE",
            response.getBody().code()
        );
    }
}
