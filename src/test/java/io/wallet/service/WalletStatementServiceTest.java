package io.wallet.service;

import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatementResponse;
import io.wallet.entity.WalletStatus;
import io.wallet.exception.InvalidDateRangeException;
import io.wallet.repository.LedgerTransactionRepository;
import io.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WalletStatementServiceTest {

    private final WalletRepository walletRepository =
        mock(WalletRepository.class);

    private final LedgerTransactionRepository ledgerRepository =
        mock(LedgerTransactionRepository.class);

    private final WalletStatementService service =
        new WalletStatementService(
            walletRepository,
            ledgerRepository
        );

    @Test
    void shouldCalculateRunningBalance() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet =
            new Wallet(1_000L, WalletStatus.ACTIVE);

        setWalletId(wallet, walletId);

        Instant from =
            Instant.parse("2026-01-01T00:00:00Z");

        Instant to =
            Instant.parse("2026-01-02T00:00:00Z");

        UUID transferOne = UUID.randomUUID();
        UUID transferTwo = UUID.randomUUID();

        LedgerTransaction credit =
            new LedgerTransaction(
                walletId,
                transferOne,
                LedgerEntryType.CREDIT,
                500L
            );

        LedgerTransaction debit =
            new LedgerTransaction(
                walletId,
                transferTwo,
                LedgerEntryType.DEBIT,
                200L
            );

        when(walletRepository.findById(walletId))
            .thenReturn(Optional.of(wallet));

        when(
            ledgerRepository.calculateOpeningBalancePaise(
                walletId,
                from
            )
        ).thenReturn(Optional.of(1_000L));

        when(
            ledgerRepository.findStatementEntries(
                walletId,
                from,
                to
            )
        ).thenReturn(List.of(credit, debit));

        WalletStatementResponse response =
            service.getStatement(
                walletId,
                from,
                to
            );

        assertEquals(1_000L, response.openingBalancePaise());
        assertEquals(1_300L, response.closingBalancePaise());
        assertEquals(2, response.entries().size());
    }

    @Test
    void shouldUseZeroOpeningBalanceWhenNoPreviousEntriesExist() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet =
            new Wallet(0L, WalletStatus.ACTIVE);

        setWalletId(wallet, walletId);

        Instant from =
            Instant.parse("2026-01-01T00:00:00Z");

        Instant to =
            Instant.parse("2026-01-02T00:00:00Z");

        when(walletRepository.findById(walletId))
            .thenReturn(Optional.of(wallet));

        when(
            ledgerRepository.calculateOpeningBalancePaise(
                walletId,
                from
            )
        ).thenReturn(Optional.empty());

        when(
            ledgerRepository.findStatementEntries(
                walletId,
                from,
                to
            )
        ).thenReturn(List.of());

        WalletStatementResponse response =
            service.getStatement(
                walletId,
                from,
                to
            );

        assertEquals(0L, response.openingBalancePaise());
        assertEquals(0L, response.closingBalancePaise());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet =
            new Wallet(0L, WalletStatus.ACTIVE);

        setWalletId(wallet, walletId);

        Instant from =
            Instant.parse("2026-01-02T00:00:00Z");

        Instant to =
            Instant.parse("2026-01-01T00:00:00Z");

        when(walletRepository.findById(walletId))
            .thenReturn(Optional.of(wallet));

        assertThrows(
            InvalidDateRangeException.class,
            () -> service.getStatement(
                walletId,
                from,
                to
            )
        );
    }

    private void setWalletId(
        Wallet wallet,
        UUID walletId
    ) {
        try {
            var field =
                Wallet.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(wallet, walletId);

        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                "Unable to prepare wallet test fixture",
                exception
            );
        }
    }
}
