package io.wallet.controller;

import io.wallet.entity.WalletResponse;
import io.wallet.entity.WalletStatementResponse;
import io.wallet.service.WalletService;
import io.wallet.service.WalletStatementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final WalletStatementService walletStatementService;

    public WalletController(
        WalletService walletService,
        WalletStatementService walletStatementService
    ) {
        this.walletService = walletService;
        this.walletStatementService = walletStatementService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(
        @PathVariable UUID id
    ) {
        WalletResponse response =
            walletService.getWallet(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<WalletStatementResponse> getStatement(
        @PathVariable UUID id,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant from,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant to
    ) {
        WalletStatementResponse response =
            walletStatementService.getStatement(
                id,
                from,
                to
            );

        return ResponseEntity.ok(response);
    }
}
