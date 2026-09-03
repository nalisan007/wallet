package io.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.TransferRequest;
import io.wallet.exception.IdempotencyKeyReuseException;
import io.wallet.repository.IdempotencyRecordRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdempotencyServiceTest {

    private final IdempotencyRecordRepository repository =
        mock(IdempotencyRecordRepository.class);

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-09-03T12:00:00Z"),
        ZoneOffset.UTC
    );

    private final IdempotencyService service =
        new IdempotencyService(repository, new ObjectMapper(), clock);

    @Test
    void shouldCreateRecord() {
        UUID key = UUID.randomUUID();
        TransferRequest request = new TransferRequest(
            UUID.randomUUID(), UUID.randomUUID(), 1_000L
        );

        final String[] hash = new String[1];
        when(repository.insertIfAbsent(eq(key), any(), any()))
            .thenAnswer(invocation -> {
                hash[0] = invocation.getArgument(1);
                return 1;
            });
        when(repository.findById(key)).thenAnswer(invocation ->
            Optional.of(new IdempotencyRecord(key, hash[0]))
        );

        IdempotencyService.Result result = service.findExistingOrCreate(key, request);

        assertTrue(result.created());
        assertEquals(key, result.record().getIdempotencyKey());
        verify(repository).insertIfAbsent(eq(key), any(), eq(clock.instant()));
    }

    @Test
    void shouldRejectDifferentRequestForExistingKey() {
        UUID key = UUID.randomUUID();
        TransferRequest first = new TransferRequest(
            UUID.randomUUID(), UUID.randomUUID(), 1_000L
        );
        TransferRequest second = new TransferRequest(
            first.fromWalletId(), first.toWalletId(), 2_000L
        );

        when(repository.insertIfAbsent(eq(key), any(), any())).thenReturn(0);
        when(repository.findById(key)).thenReturn(Optional.of(
            new IdempotencyRecord(key, "not-the-second-request-hash")
        ));

        assertThrows(
            IdempotencyKeyReuseException.class,
            () -> service.findExistingOrCreate(key, second)
        );
    }
}
