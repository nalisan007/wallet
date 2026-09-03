package io.wallet.service;

import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.TransferRequest;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.repository.LedgerTransactionRepository;
import io.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TransferConcurrencyIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LedgerTransactionRepository ledgerTransactionRepository;

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void concurrentTransfersMustNeverMakeBalanceNegative()
        throws Exception {

        TestWallets wallets = createWallets();

        long initialSenderBalance = 10_000L;
        long transferAmount = 6_000L;

        TransferRequest requestOne = new TransferRequest(
            wallets.senderId(),
            wallets.receiverOneId(),
            transferAmount
        );

        TransferRequest requestTwo = new TransferRequest(
            wallets.senderId(),
            wallets.receiverTwoId(),
            transferAmount
        );

        CountDownLatch startLatch = new CountDownLatch(1);

        try (ExecutorService executor =
                 Executors.newFixedThreadPool(2)) {

            Future<Boolean> first =
                executor.submit(() -> {
                    startLatch.await();

                    return execute
