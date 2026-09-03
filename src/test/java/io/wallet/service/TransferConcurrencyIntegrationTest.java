package io.wallet.service;

import io.wallet.entity.TransferRequest;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.repository.IdempotencyRecordRepository;
import io.wallet.repository.LedgerTransactionRepository;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

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

    @Autowired private WalletRepository walletRepository;
    @Autowired private TransferRepository transferRepository;
    @Autowired private LedgerTransactionRepository ledgerTransactionRepository;
    @Autowired private IdempotencyRecordRepository idempotencyRecordRepository;
    @Autowired private TransferService transferService;
    @Autowired private TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanDatabase() {
        idempotencyRecordRepository.deleteAll();
        ledgerTransactionRepository.deleteAll();
        transferRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void concurrentTransfersMustNeverMakeBalanceNegative() throws Exception {
        TestWallets wallets = createWallets();
        TransferRequest one = new TransferRequest(wallets.senderId(), wallets.receiverOneId(), 6_000L);
        TransferRequest two = new TransferRequest(wallets.senderId(), wallets.receiverTwoId(), 6_000L);

        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = executor.submit(() -> execute(start, one));
            Future<Boolean> second = executor.submit(() -> execute(start, two));
            start.countDown();

            assertTrue(first.get() ^ second.get());
        }

        Wallet sender = walletRepository.findById(wallets.senderId()).orElseThrow();
        assertEquals(4_000L, sender.getBalancePaise());
        assertEquals(1L, transferRepository.count());
        assertEquals(2L, ledgerTransactionRepository.count());
    }

    private boolean execute(CountDownLatch start, TransferRequest request) throws InterruptedException {
        start.await();
        try {
            transferService.createTransfer(UUID.randomUUID(), request);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private TestWallets createWallets() {
        return transactionTemplate.execute(status -> {
            Wallet sender = walletRepository.save(new Wallet(10_000L, WalletStatus.ACTIVE));
            Wallet receiverOne = walletRepository.save(new Wallet(0L, WalletStatus.ACTIVE));
            Wallet receiverTwo = walletRepository.save(new Wallet(0L, WalletStatus.ACTIVE));
            walletRepository.flush();
            return new TestWallets(sender.getId(), receiverOne.getId(), receiverTwo.getId());
        });
    }

    private record TestWallets(UUID senderId, UUID receiverOneId, UUID receiverTwoId) {}
}
