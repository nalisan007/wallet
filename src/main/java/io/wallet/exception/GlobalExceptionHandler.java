package io.wallet.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult()
            .getFieldErrors()) {

            errors.put(
                fieldError.getField(),
                fieldError.getDefaultMessage()
            );
        }

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Request validation failed",
            errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
        ConstraintViolationException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
            errors.put(
                violation.getPropertyPath().toString(),
                violation.getMessage()
            )
        );

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Request validation failed",
            errors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_PARAMETER",
            "Invalid value for parameter: "
                + exception.getName(),
            null
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
        HttpMessageNotReadableException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST_BODY",
            "Request body is malformed or contains an invalid value",
            null
        );
    }

    @ExceptionHandler(InvalidIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidIdempotencyKey(
        InvalidIdempotencyKeyException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_IDEMPOTENCY_KEY",
            exception.getMessage(),
            null
        );
    }

    @ExceptionHandler(IdempotencyKeyReuseException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyReuse(
        IdempotencyKeyReuseException exception
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "IDEMPOTENCY_KEY_REUSE",
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException exception
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "DATA_INTEGRITY_VIOLATION",
            "The operation violates a database constraint",
            null
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
        IllegalArgumentException exception
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            exception.getMessage(),
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
        Map<String, String> details
    ) {
        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            status.value(),
            code,
            message,
            details
        );

        return ResponseEntity
            .status(status)
            .body(response);
    }
}
