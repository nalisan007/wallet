package io.wallet.service;

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
import static org.mockito.Mockito.*;

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
            Instant.parse(
                "2026-09-03T10:00:00Z"
            );

        Instant to =
            Instant.parse(
                "2026-09-03T09:00:00Z"
            );

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

        verifyNoInteractions(
            transferRepository,
            cursorService
        );
    }

    @Test
    void shouldReturnNextCursor() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet =
            mock(Wallet.class);

        when(walletRepository.findById(walletId))
            .thenReturn(Optional.of(wallet));

        Transfer first =
            mock(Transfer.class);

        Transfer second =
            mock(Transfer.class);

        Transfer third =
            mock(Transfer.class);

        when(first.getId())
            .thenReturn(UUID.randomUUID());

        when(second.getId())
            .thenReturn(UUID.randomUUID());

        when(third.getId())
            .thenReturn(UUID.randomUUID());

        when(first.getCreatedAt())
            .thenReturn(
                Instant.parse(
                    "2026-09-03T10:00:00Z"
                )
            );

        when(second.getCreatedAt())
            .thenReturn(
                Instant.parse(
                    "2026-09-03T09:00:00Z"
                )
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
        ).thenReturn(
            List.of(
                first,
                second,
                third
            )
        );

        when(cursorService.encode(any()))
            .thenReturn("cursor-2");

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
            "cursor-2",
            response.nextCursor()
        );

        assertEquals(
            true,
            response.hasNext()
        );
    }
}
