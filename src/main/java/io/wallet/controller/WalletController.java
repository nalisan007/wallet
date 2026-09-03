package io.wallet.controller;

import io.wallet.entity.WalletDetailsResponse;
import io.wallet.entity.Wallet;
import io.wallet.service.WalletService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@Validated
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletDetailsResponse> getWallet(
        @PathVariable("id")
        @NotNull(message = "Wallet ID is required")
        UUID walletId
    ) {
        Wallet wallet = walletService.getWallet(walletId);

        WalletDetailsResponse response = new WalletDetailsResponse(
            wallet.getId(),
            wallet.getWalletType(),
            wallet.getBalancePaise(),
            wallet.getStatus(),
            java.util.List.of()
        );

        return ResponseEntity.ok(response);
    }
}
