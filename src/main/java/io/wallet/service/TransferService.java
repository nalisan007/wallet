package io.wallet.service;

import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.Transfer;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.entity.Wallet;
import io.wallet.exception.InsufficientBalanceException;
import io.wallet.exception.SelfTransferException;
import io.wallet.exception.WalletInactiveException;
import io.wallet.exception.WalletNotFoundException;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final IdempotencyService idempotencyService;

    public TransferService(
        TransferRepository transferRepository,
        WalletRepository walletRepository,
        IdempotencyService idempotencyService
    ) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public TransferResponse createTransfer(
        UUID idempotencyKey,
        TransferRequest request
    ) {
        IdempotencyRecord idempotencyRecord =
            idempotencyService.findExistingOrCreate(
                idempotencyKey,
                request
            );

        if (idempotencyRecord.getTransferId() != null) {
            return transferRepository
                .findById(idempotencyRecord.getTransferId())
                .map(this::toResponse)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Idempotent transfer result is missing"
                    )
                );
        }

        UUID fromWalletId = request.fromWalletId();
        UUID toWalletId = request.toWalletId();
        long amountPaise = request.amountPaise();

        if (fromWalletId.equals(toWalletId)) {
            throw new SelfTransferException(fromWalletId);
        }

        UUID firstWalletId = fromWalletId.compareTo(toWalletId) < 0
            ? fromWalletId
            : toWalletId;

        UUID secondWalletId = fromWalletId.compareTo(toWalletId) < 0
            ? toWalletId
            : fromWalletId;

        Wallet firstWallet = walletRepository
            .findByIdForUpdate(firstWalletId)
            .orElseThrow(() ->
                new WalletNotFoundException(firstWalletId)
            );

        Wallet secondWallet = walletRepository
            .findByIdForUpdate(secondWalletId)
            .orElseThrow(() ->
                new WalletNotFoundException(secondWalletId)
            );

        Wallet fromWallet = fromWalletId.equals(firstWallet.getId())
            ? firstWallet
            : secondWallet;

        Wallet toWallet = toWalletId.equals(firstWallet.getId())
            ? firstWallet
            : secondWallet;

        if (!fromWallet.getStatus().isActive()) {
            throw new WalletInactiveException(fromWallet.getId());
        }

        if (!toWallet.getStatus().isActive()) {
            throw new WalletInactiveException(toWallet.getId());
        }

        long senderBalance = fromWallet.getBalancePaise();

        if (amountPaise > senderBalance) {
            throw new InsufficientBalanceException(
                fromWallet.getId(),
                amountPaise,
                senderBalance
            );
        }

        fromWallet.debit(amountPaise);
        toWallet.credit(amountPaise);

        Transfer transfer = new Transfer(
            fromWalletId,
            toWalletId,
            amountPaise
        );

        Transfer savedTransfer =
            transferRepository.save(transfer);

        idempotencyRecord.setTransferId(savedTransfer.getId());

        return toResponse(savedTransfer);
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getFromWalletId(),
            transfer.getToWalletId(),
            transfer.getAmountPaise(),
            transfer.getCreatedAt()
        );
    }
}
