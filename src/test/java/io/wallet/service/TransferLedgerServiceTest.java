package io.wallet.service;

import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.Transfer;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.repository.LedgerTransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransferLedgerServiceTest {

    private final LedgerTransactionRepository ledgerRepository =
        mock(LedgerTransactionRepository.class);

    private final TransferLedgerService service =
        new TransferLedgerService(ledgerRepository);

    @Test
    void shouldDebitAndCreditWallets() {
        Wallet fromWallet =
            new Wallet(10_000L, WalletStatus.ACTIVE);

        Wallet toWallet =
            new Wallet(5_000L, WalletStatus.ACTIVE);

        setWalletId(fromWallet, UUID.randomUUID());
        setWalletId(toWallet, UUID.randomUUID());

        Transfer transfer =
            service.executeTransfer(
                fromWallet,
                toWallet,
                2_500L
            );

        assertEquals(
            7_500L,
            fromWallet.getBalancePaise()
        );

        assertEquals(
            7_500L,
            toWallet.getBalancePaise()
        );

        /*
         * @PrePersist normally generates the transfer ID when the
         * entity is persisted. For this unit test, persistence is
         * intentionally not performed, so ledger persistence is
         * tested separately after the transfer receives its ID.
         */
        assertEquals(
            fromWallet.getId(),
            transfer.getFromWalletId()
        );

        assertEquals(
            toWallet.getId(),
            transfer.getToWalletId()
        );

        assertEquals(
            2_500L,
            transfer.getAmountPaise()
        );
    }

    @Test
    void shouldCreateBalancedLedgerEntries() {
        UUID transferId = UUID.randomUUID();
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        Transfer transfer =
            new Transfer(
                fromWalletId,
                toWalletId,
                2_500L
            );

        setTransferId(transfer, transferId);

        service.createLedgerEntries(transfer);

        verify(ledgerRepository).save(
            org.mockito.ArgumentMatchers.argThat(
                entry ->
                    entry.getWalletId().equals(fromWalletId)
                        && entry.getTransferId().equals(transferId)
                        && entry.getEntryType()
                            == LedgerEntryType.DEBIT
                        && entry.getAmountPaise() == 2_500L
            )
        );

        verify(ledgerRepository).save(
            org.mockito.ArgumentMatchers.argThat(
                entry ->
                    entry.getWalletId().equals(toWalletId)
                        && entry.getTransferId().equals(transferId)
                        && entry.getEntryType()
                            == LedgerEntryType.CREDIT
                        && entry.getAmountPaise() == 2_500L
            )
        );
    }

    private void setWalletId(
        Wallet wallet,
        UUID id
    ) {
        try {
            var field =
                Wallet.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(wallet, id);

        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                "Unable to prepare wallet test fixture",
                exception
            );
        }
    }

    private void setTransferId(
        Transfer transfer,
        UUID id
    ) {
        try {
            var field =
                Transfer.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(transfer, id);

        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                "Unable to prepare transfer test fixture",
                exception
            );
        }
    }
}
