package io.wallet.service;

import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.entity.WalletType;
import io.wallet.exception.WalletNotFoundException;
import io.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Transactional
    public Wallet getWalletForUpdate(UUID walletId) {
        return walletRepository.findByIdForUpdate(walletId)
            .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Transactional(readOnly = true)
    public Wallet getSystemWallet() {
        return walletRepository
            .findByWalletTypeAndStatus(
                WalletType.SYSTEM,
                WalletStatus.ACTIVE
            )
            .orElseThrow(() ->
                new WalletNotFoundException(
                    "Active system wallet not found"
                )
            );
    }
}
