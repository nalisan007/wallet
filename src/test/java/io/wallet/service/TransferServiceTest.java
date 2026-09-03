package io.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.Transfer;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.exception.IdempotencyKeyProcessingException;
import io.wallet.exception.IdempotencyKeyReuseException;
import io.wallet.exception.InsufficientBalanceException;
import io.wallet.exception.SelfTransferException;
import io.wallet.repository.IdempotencyRecordRepository;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    private final WalletRepository walletRepository = mock(WalletRepository.class);
    private final TransferRepository transferRepository = mock(TransferRepository.class);
    private final IdempotencyRecordRepository idempotencyRecordRepository =
        mock(IdempotencyRecordRepository.class);
    private final TransferLedgerService ledgerService = mock(TransferLedgerService.class);
    private final IdempotencyService idempotencyService = mock(IdempotencyService.class);

    private final TransferService service = new TransferService(
        walletRepository,
        transferRepository,
        idempotencyRecordRepository,
        ledgerService,
        idempotencyService
    );

    @Test
    void shouldRejectSelfTransfer() {
        UUID walletId = UUID.randomUUID();
        TransferRequest request = new TransferRequest(walletId, walletId, 1_000L);

        assertThrows(
            SelfTransferException.class,
            () -> service.createTransfer(UUID.randomUUID(), request)
        );

        verifyNoInteractions(walletRepository, transferRepository, ledgerService, idempotencyService);
    }

    @Test
    void shouldRejectInsufficientBalance() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        UUID key = UUID.randomUUID();

        Wallet source = mock(Wallet.class);
        Wallet destination = mock(Wallet.class);
        when(source.getId()).thenReturn(sourceId);
        when(destination.getId()).thenReturn(destinationId);
        when(source.getStatus()).thenReturn(WalletStatus.ACTIVE);
        when(destination.getStatus()).thenReturn(WalletStatus.ACTIVE);
        when(source.getBalancePaise()).thenReturn(500L);

        when(idempotencyService.findExistingOrCreate(eq(key), any()))
            .thenReturn(new IdempotencyService.Result(
                new IdempotencyRecord(key, "hash"), true
            ));
        when(walletRepository.findByIdForUpdate(sourceId)).thenReturn(Optional.of(source));
        when(walletRepository.findByIdForUpdate(destinationId)).thenReturn(Optional.of(destination));

        TransferRequest request = new TransferRequest(sourceId, destinationId, 1_000L);

        assertThrows(
            InsufficientBalanceException.class,
            () -> service.createTransfer(key, request)
        );

        verify(source, never()).debit(anyLong());
        verify(destination, never()).credit(anyLong());
        verifyNoInteractions(ledgerService);
    }

    @Test
    void shouldReplayCompletedTransfer() {
        UUID key = UUID.randomUUID();
        UUID transferId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(sourceId, destinationId, 1_000L);
        IdempotencyRecord record = new IdempotencyRecord(key, "hash");
        record.setTransferId(transferId);

        Transfer transfer = mock(Transfer.class);
        when(transfer.getId()).thenReturn(transferId);
        when(transfer.getFromWalletId()).thenReturn(sourceId);
        when(transfer.getToWalletId()).thenReturn(destinationId);
        when(transfer.getAmountPaise()).thenReturn(1_000L);

        when(idempotencyService.findExistingOrCreate(eq(key), any()))
            .thenReturn(new IdempotencyService.Result(record, false));
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer));

        TransferResponse response = service.createTransfer(key, request);

        assertEquals(transferId, response.id());
        verifyNoInteractions(walletRepository, ledgerService);
    }

    @Test
    void shouldRejectDifferentRequestForExistingKey() {
        UUID key = UUID.randomUUID();
        TransferRequest request = new TransferRequest(
            UUID.randomUUID(), UUID.randomUUID(), 1_000L
        );

        when(idempotencyService.findExistingOrCreate(eq(key), any()))
            .thenThrow(new IdempotencyKeyReuseException(key));

        assertThrows(
            IdempotencyKeyReuseException.class,
            () -> service.createTransfer(key, request)
        );

        verifyNoInteractions(walletRepository, transferRepository, ledgerService);
    }

    @Test
    void shouldRejectAlreadyProcessingRequest() {
        UUID key = UUID.randomUUID();
        TransferRequest request = new TransferRequest(
            UUID.randomUUID(), UUID.randomUUID(), 1_000L
        );
        IdempotencyRecord record = new IdempotencyRecord(key, "hash");

        when(idempotencyService.findExistingOrCreate(eq(key), any()))
            .thenReturn(new IdempotencyService.Result(record, false));
        when(idempotencyService.processing(key))
            .thenReturn(new IdempotencyKeyProcessingException(key));

        assertThrows(
            IdempotencyKeyProcessingException.class,
            () -> service.createTransfer(key, request)
        );
    }
}
