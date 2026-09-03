package io.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.TransferResponse;
import io.wallet.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @Test
    void shouldCreateTransferWithValidUuidV7IdempotencyKey()
        throws Exception {

        UUID idempotencyKey =
            UUID.fromString(
                "019c0000-0000-7000-8000-000000000001"
            );

        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();
        UUID transferId = UUID.randomUUID();

        TransferRequest request =
            new TransferRequest(
                fromWalletId,
                toWalletId,
                1_000L
            );

        TransferResponse response =
            new TransferResponse(
                transferId,
                fromWalletId,
                toWalletId,
                1_000L,
                java.time.Instant.now()
            );

        when(
            transferService.createTransfer(
                eq(idempotencyKey),
                any(TransferRequest.class)
            )
        ).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/transfers")
                    .header(
                        "Idempotency-Key",
                        idempotencyKey.toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(request)
                    )
            )
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectNonUuidV7IdempotencyKey()
        throws Exception {

        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();

        TransferRequest request =
            new TransferRequest(
                fromWalletId,
                toWalletId,
                1_000L
            );

        mockMvc.perform(
                post("/api/v1/transfers")
                    .header(
                        "Idempotency-Key",
                        UUID.randomUUID().toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(request)
                    )
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidTransferRequest()
        throws Exception {

        TransferRequest request =
            new TransferRequest(
                null,
                null,
                0L
            );

        mockMvc.perform(
                post("/api/v1/transfers")
                    .header(
                        "Idempotency-Key",
                        "019c0000-0000-7000-8000-000000000001"
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(request)
                    )
            )
            .andExpect(status().isBadRequest());
    }
}
