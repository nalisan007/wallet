package io.wallet.service;

import io.wallet.entity.Cursor;
import io.wallet.entity.Transfer;
import io.wallet.entity.TransferHistoryResponse;
import io.wallet.exception.InvalidCursorException;
import io.wallet.exception.InvalidDateRangeException;
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
public class TransferHistoryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;
    private final CursorService cursorService;

    public TransferHistoryService(
        WalletRepository walletRepository,
        TransferRepository transferRepository,
        CursorService cursorService
    ) {
        this.walletRepository = walletRepository;
        this.transferRepository = transferRepository;
        this.cursorService = cursorService;
    }

    @Transactional(readOnly = true)
    public TransferHistoryResponse getHistory(
        UUID walletId,
        Instant from,
        Instant to,
        String cursor,
        int limit
    ) {
        walletRepository.findById(walletId)
            .orElseThrow(() ->
                new WalletNotFoundException(walletId)
            );

        validateDateRange(from, to);

        int pageSize = normalizeLimit(limit);

        Cursor decodedCursor = null;

        if (cursor != null && !cursor.isBlank()) {
            try {
                decodedCursor =
                    cursorService.decode(cursor);
            } catch (RuntimeException exception) {
                throw new InvalidCursorException(
                    "Invalid transfer history cursor"
                );
            }
        }

        List<Transfer> transfers =
            transferRepository.findHistory(
                walletId,
                from,
                to,
                decodedCursor == null
                    ? null
                    : decodedCursor.createdAt(),
                decodedCursor == null
                    ? null
                    : decodedCursor.id(),
                PageRequest.of(0, pageSize + 1)
            );

        boolean hasNext =
            transfers.size() > pageSize;

        List<Transfer> page =
            hasNext
                ? transfers.subList(0, pageSize)
                : transfers;

        String nextCursor = null;

        if (hasNext && !page.isEmpty()) {
            Transfer last =
                page.get(page.size() - 1);

            nextCursor =
                cursorService.encode(
                    new Cursor(
                        last.getCreatedAt(),
                        last.getId()
                    )
                );
        }

        List<io.wallet.entity.TransferResponse> responses =
            page.stream()
                .map(transfer ->
                    new io.wallet.entity.TransferResponse(
                        transfer.getId(),
                        transfer.getFromWalletId(),
                        transfer.getToWalletId(),
                        transfer.getAmountPaise(),
                        transfer.getStatus(),
                        transfer.getCreatedAt()
                    )
                )
                .toList();

        return new TransferHistoryResponse(
            responses,
            nextCursor,
            hasNext
        );
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private void validateDateRange(
        Instant from,
        Instant to
    ) {
        if (from != null
            && to != null
            && to.isBefore(from)) {

            throw new InvalidDateRangeException(
                from,
                to
            );
        }
    }
}
