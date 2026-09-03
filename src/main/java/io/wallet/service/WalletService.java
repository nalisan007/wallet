package io.wallet.service;

import io.wallet.entity.Wallet;
import io.wallet.entity.WalletResponse;
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
    public WalletResponse getWallet(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() ->
                new WalletNotFoundException(walletId)
            );

        return new WalletResponse(
            wallet.getId(),
            wallet.getBalancePaise(),
            wallet.getStatus(),
            wallet.getCreatedAt(),
            wallet.getUpdatedAt()
        );
    }
}
