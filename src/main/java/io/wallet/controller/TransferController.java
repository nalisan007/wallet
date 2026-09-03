package io.wallet.controller;

import io.wallet.config.IdempotencyKeyValidator;
import io.wallet.entity.TransferHistoryResponse;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.service.TransferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@Validated
public class TransferController {

    private final TransferService transferService;

    public TransferController(
        TransferService transferService
    ) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(
        @RequestHeader(
            value = "Idempotency-Key",
            required = false
        )
        String idempotencyKey,

        @Valid @RequestBody TransferRequest request
    ) {
        TransferResponse response =
            transferService.createTransfer(
                IdempotencyKeyValidator.parse(idempotencyKey),
                request
            );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<TransferHistoryResponse> getTransfers(
        @RequestParam
        @NotNull(message = "Wallet ID is required")
        UUID walletId,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant to,

        @RequestParam(required = false)
        @Min(
            value = 1,
            message = "Limit must be at least 1"
        )
        @Max(
            value = 100,
            message = "Limit must not exceed 100"
        )
        Integer limit,

        @RequestParam(required = false)
        String cursor
    ) {
        TransferHistoryResponse response =
            transferService.getTransfers(
                walletId,
                from,
                to,
                limit,
                cursor
            );

        return ResponseEntity.ok(response);
    }
}
