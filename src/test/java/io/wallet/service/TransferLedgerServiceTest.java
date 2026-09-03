package io.wallet.service;


import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.Transfer;
import io.wallet.repository.LedgerTransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferLedgerServiceTest {

    private final LedgerTransactionRepository ledgerRepository =
            mock(LedgerTransactionRepository.class);

    private final TransferLedgerService service =
            new TransferLedgerService(ledgerRepository);

    @Test
    void shouldCreateDebitAndCreditLedgerEntries() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        Transfer transfer =
                new Transfer(
                        fromWalletId,
                        toWalletId,
                        1_000L
                );

        service.createLedgerEntries(transfer);

        verify(ledgerRepository, times(2))
                .save(any(LedgerTransaction.class));

        var captor =
                org.mockito.ArgumentCaptor.forClass(
                        LedgerTransaction.class
                );

        verify(ledgerRepository, times(2))
                .save(captor.capture());

        List<LedgerTransaction> entries =
                captor.getAllValues();

        assertEquals(2, entries.size());

        LedgerTransaction debit =
                entries.stream()
                        .filter(entry ->
                                entry.getEntryType()
                                        == LedgerEntryType.DEBIT
                        )
                        .findFirst()
                        .orElseThrow();

        LedgerTransaction credit =
                entries.stream()
                        .filter(entry ->
                                entry.getEntryType()
                                        == LedgerEntryType.CREDIT
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                fromWalletId,
                debit.getWalletId()
        );

        assertEquals(
                toWalletId,
                credit.getWalletId()
        );

        assertEquals(
                transfer.getId(),
                debit.getTransferId()
        );

        assertEquals(
                transfer.getId(),
                credit.getTransferId()
        );

        assertEquals(
                1_000L,
                debit.getAmountPaise()
        );

        assertEquals(
                1_000L,
                credit.getAmountPaise()
        );
    }

    @Test
    void shouldCreateExactlyOneDebitAndOneCredit() {
        Transfer transfer =
                new Transfer(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        5_000L
                );

        service.createLedgerEntries(transfer);

        var captor =
                org.mockito.ArgumentCaptor.forClass(
                        LedgerTransaction.class
                );

        verify(ledgerRepository, times(2))
                .save(captor.capture());

        List<LedgerTransaction> entries =
                captor.getAllValues();

        long debitCount =
                entries.stream()
                        .filter(entry ->
                                entry.getEntryType()
                                        == LedgerEntryType.DEBIT
                        )
                        .count();

        long creditCount =
                entries.stream()
                        .filter(entry ->
                                entry.getEntryType()
                                        == LedgerEntryType.CREDIT
                        )
                        .count();

        assertEquals(1, debitCount);
        assertEquals(1, creditCount);
    }
}
