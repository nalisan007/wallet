package io.wallet.service;

import io.wallet.entity.Wallet;
import io.wallet.entity.WalletResponse;
import io.wallet.exception.WalletNotFoundException;
import io.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {
        Optional<Wallet> wallet =
            walletRepository.findById(walletId);

        return wallet
            .map(this::toResponse)
            .orElseThrow(() ->
                new WalletNotFoundException(walletId)
            );
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
            wallet.getId(),
            wallet.getBalancePaise(),
            wallet.getStatus().name(),
            wallet.getCreatedAt()
        );
    }
}
