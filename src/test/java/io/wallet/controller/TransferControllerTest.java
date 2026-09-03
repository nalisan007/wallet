package io.wallet.controller;

import com.github.f4b6a3.uuid.UuidCreator;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.entity.TransferStatus;
import io.wallet.exception.GlobalExceptionHandler;
import io.wallet.service.TransferHistoryService;
import io.wallet.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerTest {

    private final TransferService transferService =
        mock(TransferService.class);

    private final TransferHistoryService historyService =
        mock(TransferHistoryService.class);

    private final TransferController controller =
        new TransferController(
            transferService,
            historyService
        );

    private final MockMvc mockMvc =
        MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void shouldCreateTransfer() throws Exception {
        UUID walletId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        UUID idempotencyKey = UuidCreator.getTimeOrderedEpoch();

        TransferResponse response =
            new TransferResponse(
                UUID.randomUUID(),
                walletId,
                destinationId,
                1_000L,
                TransferStatus.COMPLETED,
                Instant.now()
            );

        org.mockito.Mockito.when(
            transferService.createTransfer(
                eq(idempotencyKey),
                any(TransferRequest.class)
            )
        ).thenReturn(response);

        mockMvc.perform(
            post("/api/v1/wallets/{walletId}/transfers", walletId)
                .header(
                    "Idempotency-Key",
                    idempotencyKey.toString()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fromWalletId": "%s",
                      "toWalletId": "%s",
                      "amountPaise": 1000
                    }
                    """.formatted(
                        walletId,
                        destinationId
                    ))
        )
        .andExpect(status().isCreated());

        verify(transferService).createTransfer(
            eq(idempotencyKey),
            any(TransferRequest.class)
        );
    }

    @Test
    void shouldRejectMismatchedUrlAndBodyWalletId()
        throws Exception {

        UUID urlWalletId = UUID.randomUUID();
        UUID bodyWalletId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        mockMvc.perform(
            post(
                "/api/v1/wallets/{walletId}/transfers",
                urlWalletId
            )
                .header(
                    "Idempotency-Key",
                    UuidCreator.getTimeOrderedEpoch().toString()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fromWalletId": "%s",
                      "toWalletId": "%s",
                      "amountPaise": 1000
                    }
                    """.formatted(
                        bodyWalletId,
                        destinationId
                    ))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidIdempotencyKey()
        throws Exception {

        UUID walletId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        mockMvc.perform(
            post(
                "/api/v1/wallets/{walletId}/transfers",
                walletId
            )
                .header(
                    "Idempotency-Key",
                    "not-a-uuid"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fromWalletId": "%s",
                      "toWalletId": "%s",
                      "amountPaise": 1000
                    }
                    """.formatted(
                        walletId,
                        destinationId
                    ))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidAmount()
        throws Exception {

        UUID walletId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        mockMvc.perform(
            post(
                "/api/v1/wallets/{walletId}/transfers",
                walletId
            )
                .header(
                    "Idempotency-Key",
                    UuidCreator.getTimeOrderedEpoch().toString()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fromWalletId": "%s",
                      "toWalletId": "%s",
                      "amountPaise": 0
                    }
                    """.formatted(
                        walletId,
                        destinationId
                    ))
        )
        .andExpect(status().isBadRequest());
    }
}
