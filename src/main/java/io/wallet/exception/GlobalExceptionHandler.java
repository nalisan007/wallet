package io.wallet.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                errors.put(
                    error.getField(),
                    error.getDefaultMessage()
                )
            );

        exception.getBindingResult()
            .getGlobalErrors()
            .forEach(error ->
                errors.put(
                    error.getObjectName(),
                    error.getDefaultMessage()
                )
            );

        return response(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Request validation failed",
            errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
        ConstraintViolationException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations()
            .forEach(violation ->
                errors.put(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
                )
            );

        return response(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Request validation failed",
            errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequest(
        HttpMessageNotReadableException exception
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            "MALFORMED_REQUEST",
            "Request body is invalid",
            Map.of()
        );
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiError> handleWalletNotFound(
        WalletNotFoundException exception
    ) {
        return response(
            HttpStatus.NOT_FOUND,
            "WALLET_NOT_FOUND",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(WalletInactiveException.class)
    public ResponseEntity<ApiError> handleWalletInactive(
        WalletInactiveException exception
    ) {
        return response(
            HttpStatus.CONFLICT,
            "WALLET_INACTIVE",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> handleInsufficientBalance(
        InsufficientBalanceException exception
    ) {
        return response(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "INSUFFICIENT_BALANCE",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(SelfTransferException.class)
    public ResponseEntity<ApiError> handleSelfTransfer(
        SelfTransferException exception
    ) {
        return response(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "SELF_TRANSFER",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(IdempotencyKeyReuseException.class)
    public ResponseEntity<ApiError> handleIdempotencyKeyReuse(
        IdempotencyKeyReuseException exception
    ) {
        return response(
            HttpStatus.CONFLICT,
            "IDEMPOTENCY_KEY_REUSE",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(InvalidIdempotencyKeyException.class)
    public ResponseEntity<ApiError> handleInvalidIdempotencyKey(
        InvalidIdempotencyKeyException exception
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            "INVALID_IDEMPOTENCY_KEY",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(InvalidCursorException.class)
    public ResponseEntity<ApiError> handleInvalidCursor(
        InvalidCursorException exception
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            "INVALID_CURSOR",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiError> handleInvalidDateRange(
        InvalidDateRangeException exception
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            "INVALID_DATE_RANGE",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
        IllegalArgumentException exception
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            "BAD_REQUEST",
            exception.getMessage(),
            Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
        Exception exception
    ) {
        return response(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            Map.of()
        );
    }

    private ResponseEntity<ApiError> response(
        HttpStatus status,
        String code,
        String message,
        Map<String, String> details
    ) {
        return ResponseEntity
            .status(status)
            .body(
                new ApiError(
                    code,
                    message,
                    details,
                    Instant.now()
                )
            );
    }

    public record ApiError(
        String code,
        String message,
        Map<String, String> details,
        Instant timestamp
    ) {
    }
}
