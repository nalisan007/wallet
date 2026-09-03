package io.wallet.controller;

import io.wallet.entity.WalletResponse;
import io.wallet.entity.WalletStatementResponse;
import io.wallet.service.WalletService;
import io.wallet.service.WalletStatementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private WalletStatementService walletStatementService;

    @Test
    void shouldGetWallet() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletResponse response =
            new WalletResponse(
                walletId,
                10_000L,
                "ACTIVE",
                Instant.now()
            );

        when(walletService.getWallet(walletId))
            .thenReturn(response);

        mockMvc.perform(
                get("/api/v1/wallets/{id}", walletId)
            )
            .andExpect(status().isOk());
    }

    @Test
    void shouldGetWalletStatement() throws Exception {

        UUID walletId = UUID.randomUUID();

        Instant from =
            Instant.parse("2026-01-01T00:00:00Z");

        Instant to =
            Instant.parse("2026-01-31T23:59:59Z");

        WalletStatementResponse response =
            new WalletStatementResponse(
                walletId,
                from,
                to,
                10_000L,
                List.of(),
                10_000L
            );

        when(
            walletStatementService.getStatement(
                eq(walletId),
                eq(from),
                eq(to)
            )
        ).thenReturn(response);

        mockMvc.perform(
                get(
                    "/api/v1/wallets/{id}/statement",
                    walletId
                )
                    .param("from", from.toString())
                    .param("to", to.toString())
            )
            .andExpect(status().isOk());
    }
}
