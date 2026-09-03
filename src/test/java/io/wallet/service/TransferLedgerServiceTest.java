package io.wallet.service;

import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.Transfer;
import io.wallet.repository.LedgerTransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransferLedgerServiceTest {

    @Test
    void shouldCreateBalancedDebitAndCreditEntries() {
        LedgerTransactionRepository repository =
            mock(LedgerTransactionRepository.class);

        TransferLedgerService service =
            new TransferLedgerService(repository);

        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        Transfer transfer = new Transfer(
            fromWalletId,
            toWalletId,
            10_050L
        );

        service.createLedgerEntries(transfer);

        ArgumentCaptor<LedgerTransaction> captor =
            ArgumentCaptor.forClass(LedgerTransaction.class);

        verify(repository, times(2)).save(captor.capture());

        List<LedgerTransaction> entries =
            captor.getAllValues();

        assertEquals(2, entries.size());

        LedgerTransaction debit = entries.stream()
            .filter(entry ->
                entry.getEntryType() == LedgerEntryType.DEBIT
            )
            .findFirst()
            .orElseThrow();

        LedgerTransaction credit = entries.stream()
            .filter(entry ->
                entry.getEntryType() == LedgerEntryType.CREDIT
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
            10_050L,
            debit.getAmountPaise()
        );

        assertEquals(
            10_050L,
            credit.getAmountPaise()
        );

        assertEquals(
            transfer.getId(),
            debit.getTransferId()
        );

        assertEquals(
            transfer.getId(),
            credit.getTransferId()
        );
    }
}
