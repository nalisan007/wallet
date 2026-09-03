package io.wallet.service;

import io.wallet.entity.Cursor;
import io.wallet.entity.Transfer;
import io.wallet.entity.TransferHistoryResponse;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.exception.InvalidDateRangeException;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransferHistoryServiceTest {

    private final WalletRepository walletRepository =
        mock(WalletRepository.class);

    private final TransferRepository transferRepository =
        mock(TransferRepository.class);

    private final CursorService cursorService =
        mock(CursorService.class);

    private final TransferHistoryService service =
        new TransferHistoryService(
            walletRepository,
            transferRepository,
            cursorService
        );

    @Test
    void shouldRejectInvalidDateRange() {
        UUID walletId = UUID.randomUUID();

        when(walletRepository.findById(walletId))
            .thenReturn(
                Optional.of(
                    new Wallet(
                        10_000L,
                        WalletStatus.ACTIVE
                    )
                )
            );

        Instant from =
            Instant.parse("2026-02-02T00:00:00Z");

        Instant to =
            Instant.parse("2026-02-01T00:00:00Z");

        assertThrows(
            InvalidDateRangeException.class,
            () -> service.getHistory(
                walletId,
                from,
                to,
                null,
                20
            )
        );
    }

    @Test
    void shouldReturnNextCursorWhenMoreRecordsExist() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet =
            new Wallet(
                10_000L,
                WalletStatus.ACTIVE
            );

        setWalletId(wallet, walletId);

        when(walletRepository.findById(walletId))
            .thenReturn(Optional.of(wallet));

        List<Transfer> transfers = List.of(
            createTransfer(walletId),
            createTransfer(walletId),
            createTransfer(walletId)
        );

        when(
            transferRepository.findHistory(
                eq(walletId),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any()
            )
        ).thenReturn(transfers);

        when(cursorService.encode(any()))
            .thenReturn("next-cursor");

        TransferHistoryResponse response =
            service.getHistory(
                walletId,
                null,
                null,
                null,
                2
            );

        assertEquals(
            2,
            response.transfers().size()
        );

        assertEquals(
            "next-cursor",
            response.nextCursor()
        );

        assertEquals(
            true,
            response.hasNext()
        );
    }

    private Transfer createTransfer(
        UUID walletId
    ) {
        UUID destination = UUID.randomUUID();

        return new Transfer(
            walletId,
            destination,
            1_000L
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
}
