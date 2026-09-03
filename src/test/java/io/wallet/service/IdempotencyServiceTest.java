package io.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.TransferRequest;
import io.wallet.exception.IdempotencyKeyReuseException;
import io.wallet.repository.IdempotencyRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdempotencyServiceTest {

    private final IdempotencyRecordRepository repository =
        mock(IdempotencyRecordRepository.class);

    private final IdempotencyService service =
        new IdempotencyService(
            repository,
            new ObjectMapper()
        );

    @Test
    void shouldCreateNewRecord() {
        UUID key = UUID.randomUUID();

        TransferRequest request =
            new TransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1_000L
            );

        when(repository.findById(key))
            .thenReturn(Optional.empty());

        when(repository.saveAndFlush(any()))
            .thenAnswer(invocation ->
                invocation.getArgument(0));

        IdempotencyRecord result =
            service.findExistingOrCreate(
                key,
                request
            );

        assertEquals(
            key,
            result.getIdempotencyKey()
        );

        verify(repository).saveAndFlush(any());
    }

    @Test
    void shouldReturnExistingRecordForSameRequest() {
        UUID key = UUID.randomUUID();

        TransferRequest request =
            new TransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1_000L
            );

        when(repository.findById(key))
            .thenReturn(
                Optional.of(
                    createRecord(
                        key,
                        request
                    )
                )
            );

        IdempotencyRecord result =
            service.findExistingOrCreate(
                key,
                request
            );

        assertEquals(
            key,
            result.getIdempotencyKey()
        );

        verify(repository, never())
            .saveAndFlush(any());
    }

    @Test
    void shouldRejectSameKeyWithDifferentRequest() {
        UUID key = UUID.randomUUID();

        TransferRequest original =
            new TransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1_000L
            );

        TransferRequest different =
            new TransferRequest(
                original.fromWalletId(),
                original.toWalletId(),
                2_000L
            );

        when(repository.findById(key))
            .thenReturn(
                Optional.of(
                    createRecord(
                        key,
                        original
                    )
                )
            );

        assertThrows(
            IdempotencyKeyReuseException.class,
            () -> service.findExistingOrCreate(
                key,
                different
            )
        );

        verify(repository, never())
            .saveAndFlush(any());
    }

    private IdempotencyRecord createRecord(
        UUID key,
        TransferRequest request
    ) {
        IdempotencyService hashService =
            new IdempotencyService(
                mock(IdempotencyRecordRepository.class),
                new ObjectMapper()
            );

        return hashService.findExistingOrCreate(
            key,
            request
        );
    }
}
