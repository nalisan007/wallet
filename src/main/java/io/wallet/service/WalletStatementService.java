package io.wallet.service;

import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.StatementEntryResponse;
import io.wallet.entity.LedgerTransactionType;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatementResponse;
import io.wallet.exception.InvalidDateRangeException;
import io.wallet.exception.WalletNotFoundException;
import io.wallet.repository.LedgerTransactionRepository;
import io.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WalletStatementService {

    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public WalletStatementService(
        WalletRepository walletRepository,
        LedgerTransactionRepository ledgerTransactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository =
            ledgerTransactionRepository;
    }


    @Transactional(readOnly = true)
    public WalletStatementResponse getStatement(
            UUID walletId,
            Instant from,
            Instant to
    ) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(walletId)
                );

        validateDateRange(from, to);

        long openingBalancePaise =
                ledgerTransactionRepository
                        .calculateOpeningBalancePaise(
                                walletId,
                                from,
                                LedgerEntryType.CREDIT
                        )
                        .orElse(0L);

        List<LedgerTransaction> transactions =
                ledgerTransactionRepository.findStatementEntries(
                        walletId,
                        from,
                        to
                );

        long runningBalancePaise = openingBalancePaise;

        List<StatementEntryResponse> entries =
                new ArrayList<>(transactions.size());

        for (LedgerTransaction transaction : transactions) {

            LedgerEntryType entryType =
                    transaction.getEntryType();

            long signedAmount =
                    entryType == LedgerEntryType.CREDIT
                            ? transaction.getAmountPaise()
                            : -transaction.getAmountPaise();

            runningBalancePaise += signedAmount;

            LedgerTransactionType transactionType;

            if (transaction.getTransferId() != null) {
                transactionType = LedgerTransactionType.TRANSFER;
            } else if (entryType == LedgerEntryType.CREDIT) {
                transactionType = LedgerTransactionType.DEPOSIT;
            } else {
                transactionType = LedgerTransactionType.WITHDRAWAL;
            }

            entries.add(
                    new StatementEntryResponse(
                            transaction.getId(),
                            transaction.getTransferId(),
                            transactionType,
                            entryType,
                            transaction.getAmountPaise(),
                            transaction.getCreatedAt()
                    )
            );
        }

        return new WalletStatementResponse(
                wallet.getId(),
                from,
                to,
                openingBalancePaise,
                List.copyOf(entries),
                runningBalancePaise
        );
    }


    private void validateDateRange(
        Instant from,
        Instant to
    ) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(
                "From and to dates are required"
            );
        }

        if (to.isBefore(from)) {
            throw new InvalidDateRangeException(from, to);
        }
    }
}
