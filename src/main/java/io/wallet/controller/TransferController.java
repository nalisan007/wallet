package io.wallet.controller;

import io.wallet.config.IdempotencyKeyValidator;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(
        @RequestHeader(value = "Idempotency-Key", required = false)
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
}
