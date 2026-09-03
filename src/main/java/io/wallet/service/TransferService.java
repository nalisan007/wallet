package io.wallet.service;

import io.wallet.entity.Cursor;
import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.Transfer;
import io.wallet.entity.TransferHistoryResponse;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.entity.Wallet;
import io.wallet.exception.InsufficientBalanceException;
import io.wallet.exception.InvalidDateRangeException;
import io.wallet.exception.SelfTransferException;
import io.wallet.exception.WalletInactiveException;
import io.wallet.exception.WalletNotFoundException;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransferService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final IdempotencyService idempotencyService;
    private final CursorService cursorService;

    public TransferService(
        TransferRepository transferRepository,
        WalletRepository walletRepository,
        IdempotencyService idempotencyService,
        CursorService cursorService
    ) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
        this.idempotencyService = idempotencyService;
        this.cursorService = cursorService;
    }

    @Transactional(readOnly = true)
    public TransferHistoryResponse getTransfers(
        UUID walletId,
        Instant from,
        Instant to,
        Integer limit,
        String cursor
    ) {
        walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException(walletId));

        validateDateRange(from, to);

        int requestedLimit = limit == null
            ? DEFAULT_LIMIT
            : limit;

        Cursor decodedCursor = cursorService.decode(cursor);

        Instant cursorCreatedAt = decodedCursor == null
            ? null
            : decodedCursor.createdAt();

        UUID cursorId = decodedCursor == null
            ? null
            : decodedCursor.id();

        List<Transfer> transfers =
            transferRepository.findWalletTransfers(
                walletId,
                from,
                to,
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, requestedLimit + 1)
            );

        boolean hasNextPage =
            transfers.size() > requestedLimit;

        if (hasNextPage) {
            transfers = transfers.subList(
                0,
                requestedLimit
            );
        }

        String nextCursor = null;

        if (hasNextPage && !transfers.isEmpty()) {
            Transfer lastTransfer =
                transfers.get(transfers.size() - 1);

            nextCursor = cursorService.encode(
                new Cursor(
                    lastTransfer.getCreatedAt(),
                    lastTransfer.getId()
                )
            );
        }

        List<TransferResponse> responses =
            transfers.stream()
                .map(this::toResponse)
                .toList();

        return new TransferHistoryResponse(
            responses,
            nextCursor
        );
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

        idempotencyRecord.setTransferId(
            savedTransfer.getId()
        );

        return toResponse(savedTransfer);
    }

    private void validateDateRange(
        Instant from,
        Instant to
    ) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new InvalidDateRangeException(from, to);
        }
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
