package io.wallet.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(
        WalletNotFoundException exception
    ) {
        return buildResponse(
            HttpStatus.NOT_FOUND,
            "WALLET_NOT_FOUND",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
        UserNotFoundException exception
    ) {
        return buildResponse(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
        InsufficientBalanceException exception
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "INSUFFICIENT_BALANCE",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(SelfTransferException.class)
    public ResponseEntity<ErrorResponse> handleSelfTransfer(
        SelfTransferException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "SELF_TRANSFER_NOT_ALLOWED",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(
        InvalidAmountException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_AMOUNT",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(
        InvalidDateRangeException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_DATE_RANGE",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(IdempotencyKeyReuseException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyReuse(
        IdempotencyKeyReuseException exception
    ) {
        return buildResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "IDEMPOTENCY_KEY_REUSED",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(IdempotencyKeyProcessingException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyProcessing(
        IdempotencyKeyProcessingException exception
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "IDEMPOTENCY_REQUEST_PROCESSING",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(InvalidCursorException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCursor(
        InvalidCursorException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_CURSOR",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(WalletInactiveException.class)
    public ResponseEntity<ErrorResponse> handleWalletInactive(
        WalletInactiveException exception
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "WALLET_INACTIVE",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                fieldErrors.put(
                    error.getField(),
                    error.getDefaultMessage()
                )
            );

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Request validation failed",
            fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
        ConstraintViolationException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getConstraintViolations()
            .forEach(violation ->
                fieldErrors.put(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
                )
            );

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Request validation failed",
            fieldErrors
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException exception
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "DATA_INTEGRITY_VIOLATION",
            "The request violates a database constraint",
            null
        );
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
        OptimisticLockException exception
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "CONCURRENT_UPDATE",
            "The resource was modified concurrently",
            null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
        Exception exception
    ) {
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            null
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
        HttpStatus status,
        String code,
        String message,
        Map<String, String> fieldErrors
    ) {
        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            status.value(),
            code,
            message,
            fieldErrors
        );

        return ResponseEntity
            .status(status)
            .body(response);
    }
}
