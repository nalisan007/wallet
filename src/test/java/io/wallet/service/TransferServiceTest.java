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
import io.wallet.repository.IdempotencyRecordRepository;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    private final WalletRepository walletRepository =
        mock(WalletRepository.class);

    private final TransferRepository transferRepository =
        mock(TransferRepository.class);

    private final IdempotencyRecordRepository
        idempotencyRecordRepository =
            mock(IdempotencyRecordRepository.class);

    private final TransferLedgerService transferLedgerService =
        mock(TransferLedgerService.class);

    private final IdempotencyService idempotencyService =
        mock(IdempotencyService.class);

    private final TransferService service =
        new TransferService(
            walletRepository,
            transferRepository,
            idempotencyRecordRepository,
            transferLedgerService,
            idempotencyService
        );

    @Test
    void shouldRejectSelfTransfer() {
        UUID walletId = UUID.randomUUID();

        TransferRequest request =
            new TransferRequest(
                walletId,
                walletId,
                1_000L
            );

        assertThrows(
            SelfTransferException.class,
            () -> service.createTransfer(
                UUID.fromString(
                    "019c0000-0000-7000-8000-000000000001"
                ),
                request
            )
        );

        verifyNoInteractions(
            walletRepository,
            transferRepository,
            idempotencyService
        );
    }

    @Test
    void shouldRejectInactiveSourceWallet() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        TransferRequest request =
            new TransferRequest(
                fromWalletId,
                toWalletId,
                1_000L
            );

        IdempotencyRecord record =
            new IdempotencyRecord(
                UUID.fromString(
                    "019c0000-0000-7000-8000-000000000001"
                ),
                "a".repeat(64)
            );

        Wallet fromWallet =
            new Wallet(10_000L, WalletStatus.SUSPENDED);

        Wallet toWallet =
            new Wallet(10_000L, WalletStatus.ACTIVE);

        setWalletId(fromWallet, fromWalletId);
        setWalletId(toWallet, toWalletId);

        when(
            idempotencyService.findExistingOrCreate(
                any(),
                eq(request)
            )
        ).thenReturn(record);

        when(walletRepository.findByIdForUpdate(fromWalletId))
            .thenReturn(Optional.of(fromWallet));

        when(walletRepository.findByIdForUpdate(toWalletId))
            .thenReturn(Optional.of(toWallet));

        assertThrows(
            WalletInactiveException.class,
            () -> service.createTransfer(
                UUID.fromString(
                    "019c0000-0000-7000-8000-000000000001"
                ),
                request
            )
        );

        verifyNoInteractions(transferLedgerService);
    }

    @Test
    void shouldRejectInsufficientBalance() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        TransferRequest request =
            new TransferRequest(
                fromWalletId,
                toWalletId,
                20_000L
            );

        IdempotencyRecord record =
            new IdempotencyRecord(
                UUID.fromString(
                    "019c0000-0000-7000-8000-000000000001"
                ),
                "a".repeat(64)
            );

        Wallet fromWallet =
            new Wallet(10_000L, WalletStatus.ACTIVE);

        Wallet toWallet =
            new Wallet(10_000L, WalletStatus.ACTIVE);

        setWalletId(fromWallet, fromWalletId);
        setWalletId(toWallet, toWalletId);

        when(
            idempotencyService.findExistingOrCreate(
                any(),
                eq(request)
            )
        ).thenReturn(record);

        when(walletRepository.findByIdForUpdate(fromWalletId))
            .thenReturn(Optional.of(fromWallet));

        when(walletRepository.findByIdForUpdate(toWalletId))
            .thenReturn(Optional.of(toWallet));

        assertThrows(
            InsufficientBalanceException.class,
            () -> service.createTransfer(
                UUID.fromString(
                    "019c0000-0000-7000-8000-000000000001"
                ),
                request
            )
        );

        verifyNoInteractions(transferLedgerService);
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
}
