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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> createTransfer(
        @RequestHeader("Idempotency-Key")
        String idempotencyKey,

        @Valid
        @RequestBody
        TransferRequest request
    ) {
        UUID parsedIdempotencyKey =
            IdempotencyKeyValidator.parse(idempotencyKey);

        TransferResponse response =
            transferService.createTransfer(
                parsedIdempotencyKey,
                request
            );

        return ResponseEntity
            .created(
                URI.create(
                    "/api/v1/transfers/" + response.id()
                )
            )
            .body(response);
    }

    @GetMapping("/wallets/{walletId}/transfers")
    public ResponseEntity<TransferHistoryResponse> getTransferHistory(
        @PathVariable
        @NotNull(message = "Wallet ID is required")
        UUID walletId,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant to,

        @RequestParam(required = false, defaultValue = "20")
        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 100, message = "Limit must not exceed 100")
        Integer limit,

        @RequestParam(required = false)
        String cursor
    ) {
        return ResponseEntity.ok(
            transferService.getTransfers(
                walletId,
                from,
                to,
                limit,
                cursor
            )
        );
    }
}
