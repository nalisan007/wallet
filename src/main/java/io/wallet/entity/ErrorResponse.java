package io.wallet.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(

    @NotNull(message = "Error timestamp is required")
    Instant timestamp,

    @NotNull(message = "HTTP status is required")
    Integer status,

    @NotBlank(message = "Error code is required")
    String code,

    @NotBlank(message = "Error message is required")
    String message,

    Map<String, String> fieldErrors

) {
}
