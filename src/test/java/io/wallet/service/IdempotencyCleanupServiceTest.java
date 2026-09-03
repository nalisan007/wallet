package io.wallet.service;

import io.wallet.repository.IdempotencyRecordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class IdempotencyCleanupServiceTest {

    private final IdempotencyRecordRepository repository =
        mock(IdempotencyRecordRepository.class);

    private final Instant now =
        Instant.parse("2026-09-03T12:00:00Z");

    private final Clock clock =
        Clock.fixed(
            now,
            ZoneOffset.UTC
        );

    private final IdempotencyCleanupService service =
        new IdempotencyCleanupService(
            repository,
            clock,
            Duration.ofHours(24)
        );

    @Test
    void shouldDeleteExpiredCompletedRecords() {
        when(
            repository.deleteExpiredCompletedRecords(any())
        ).thenReturn(7);

        int deleted = service.cleanup();

        assertEquals(7, deleted);

        ArgumentCaptor<Instant> captor =
            ArgumentCaptor.forClass(Instant.class);

        verify(repository)
            .deleteExpiredCompletedRecords(
                captor.capture()
            );

        assertEquals(
            Instant.parse("2026-09-02T12:00:00Z"),
            captor.getValue()
        );
    }
}
