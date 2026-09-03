package io.wallet.service;

import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.Transfer;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.exception.InsufficientBalanceException;
import io.wallet.exception.SelfTransferException;
import io.wallet.exception.WalletInactiveException;
import io.wallet.exception.WalletNotFoundException;
import io.wallet.repository.IdempotencyRecordRepository;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final TransferLedgerService transferLedgerService;
    private final IdempotencyService idempotencyService;

    public TransferService(
        WalletRepository walletRepository,
        TransferRepository transferRepository,
        IdempotencyRecordRepository idempotencyRecordRepository,
        TransferLedgerService transferLedgerService,
        IdempotencyService idempotencyService
    ) {
        this.walletRepository = walletRepository;
        this.transferRepository = transferRepository;
        this.idempotencyRecordRepository =
            idempotencyRecordRepository;
        this.transferLedgerService = transferLedgerService;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public TransferResponse createTransfer(
        UUID idempotencyKey,
        TransferRequest request
    ) {
        validateRequest(request);

        IdempotencyRecord record =
            idempotencyService.findExistingOrCreate(
                idempotencyKey,
                request
            );

        if (record.getTransferId() != null) {
            return transferRepository
                .findById(record.getTransferId())
                .map(this::toResponse)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Idempotency record references a missing transfer"
                    )
                );
        }

        UUID firstWalletId;
        UUID secondWalletId;

        if (request.fromWalletId()
            .toString()
            .compareTo(request.toWalletId().toString()) < 0) {

            firstWalletId = request.fromWalletId();
            secondWalletId = request.toWalletId();

        } else {
            firstWalletId = request.toWalletId();
            secondWalletId = request.fromWalletId();
        }

        Wallet firstWallet =
            walletRepository.findByIdForUpdate(firstWalletId)
                .orElseThrow(() ->
                    new WalletNotFoundException(firstWalletId)
                );

        Wallet secondWallet =
            walletRepository.findByIdForUpdate(secondWalletId)
                .orElseThrow(() ->
                    new WalletNotFoundException(secondWalletId)
                );

        Wallet fromWallet =
            firstWallet.getId().equals(request.fromWalletId())
                ? firstWallet
                : secondWallet;

        Wallet toWallet =
            firstWallet.getId().equals(request.toWalletId())
                ? firstWallet
                : secondWallet;

        validateWallets(
            fromWallet,
            toWallet,
            request.amountPaise()
        );

        Transfer transfer =
            transferLedgerService.createTransfer(
                fromWallet,
                toWallet,
                request.amountPaise()
            );

        Transfer persistedTransfer =
            transferRepository.save(transfer);

        transferLedgerService.createLedgerEntries(
            persistedTransfer
        );

        record.setTransferId(
            persistedTransfer.getId()
        );

        idempotencyRecordRepository.save(record);

        return toResponse(persistedTransfer);
    }

    private void validateRequest(
        TransferRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                "Transfer request is required"
            );
        }

        if (request.fromWalletId() == null) {
            throw new IllegalArgumentException(
                "Source wallet ID is required"
            );
        }

        if (request.toWalletId() == null) {
            throw new IllegalArgumentException(
                "Destination wallet ID is required"
            );
        }

        if (request.fromWalletId()
            .equals(request.toWalletId())) {

            throw new SelfTransferException(
                request.fromWalletId()
            );
        }

        if (request.amountPaise() == null
            || request.amountPaise() <= 0) {

            throw new IllegalArgumentException(
                "Transfer amount must be positive"
            );
        }
    }

    private void validateWallets(
        Wallet fromWallet,
        Wallet toWallet,
        long amountPaise
    ) {
        if (fromWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletInactiveException(
                fromWallet.getId()
            );
        }

        if (toWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletInactiveException(
                toWallet.getId()
            );
        }

        if (fromWallet.getBalancePaise() < amountPaise) {
            throw new InsufficientBalanceException(
                fromWallet.getId(),
                amountPaise,
                fromWallet.getBalancePaise()
            );
        }
    }

    private TransferResponse toResponse(
        Transfer transfer
    ) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getFromWalletId(),
            transfer.getToWalletId(),
            transfer.getAmountPaise(),
            transfer.getStatus(),
            transfer.getCreatedAt()
        );
    }
}
