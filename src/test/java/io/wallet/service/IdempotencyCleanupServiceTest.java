package io.wallet.service;

import io.wallet.repository.IdempotencyRecordRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdempotencyCleanupServiceTest {

    private final IdempotencyRecordRepository repository =
        mock(IdempotencyRecordRepository.class);

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-09-03T12:00:00Z"),
        ZoneOffset.UTC
    );

    private final IdempotencyCleanupService service =
        new IdempotencyCleanupService(
            repository,
            clock,
            Duration.ofHours(24)
        );

    @Test
    void shouldDeleteExpiredRecords() {
        when(repository.deleteExpiredCompletedRecords(any())).thenReturn(5);

        assertEquals(5, service.deleteExpiredRecords());

        verify(repository).deleteExpiredCompletedRecords(
            Instant.parse("2026-09-02T12:00:00Z")
        );
    }
}
