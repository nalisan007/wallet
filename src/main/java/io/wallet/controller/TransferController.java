package io.wallet.controller;

import io.wallet.entity.TransferHistoryResponse;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.config.IdempotencyKeyValidator;
import io.wallet.service.TransferHistoryService;
import io.wallet.service.TransferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@Validated
public class TransferController {

    private final TransferService transferService;
    private final TransferHistoryService transferHistoryService;

    public TransferController(
        TransferService transferService,
        TransferHistoryService transferHistoryService
    ) {
        this.transferService = transferService;
        this.transferHistoryService = transferHistoryService;
    }

    @PostMapping("/{walletId}/transfers")
    public ResponseEntity<TransferResponse> createTransfer(
        @PathVariable
        @NotNull(
            message = "Wallet ID is required"
        )
        UUID walletId,

        @RequestHeader(
            value = "Idempotency-Key",
            required = true
        )
        String idempotencyKey,

        @Valid
        @RequestBody
        TransferRequest request
    ) {
        validateWalletId(walletId, request);

        UUID parsedIdempotencyKey =
            IdempotencyKeyValidator.parse(idempotencyKey);

        TransferResponse response =
            transferService.createTransfer(
                parsedIdempotencyKey,
                request
            );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/{walletId}/transfers")
    public ResponseEntity<TransferHistoryResponse> getTransferHistory(
        @PathVariable
        @NotNull(
            message = "Wallet ID is required"
        )
        UUID walletId,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant to,

        @RequestParam(required = false)
        String cursor,

        @RequestParam(defaultValue = "20")
        @Min(
            value = 1,
            message = "Limit must be at least 1"
        )
        @Max(
            value = 100,
            message = "Limit must not exceed 100"
        )
        int limit
    ) {
        return ResponseEntity.ok(
            transferHistoryService.getHistory(
                walletId,
                from,
                to,
                cursor,
                limit
            )
        );
    }

    private void validateWalletId(
        UUID walletId,
        TransferRequest request
    ) {
        if (!walletId.equals(request.fromWalletId())) {
            throw new IllegalArgumentException(
                "Source wallet ID must match the wallet ID in the URL"
            );
        }
    }


}
