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

@SpringBootTest
@ActiveProfiles("test")
class IdempotencyConcurrencyIntegrationTest {

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
    void sameIdempotencyKeyMustMoveMoneyExactlyOnce() throws Exception {
        TestWallets wallets = createWallets();
        TransferRequest request = new TransferRequest(
            wallets.senderId(), wallets.receiverId(), 2_500L
        );
        UUID key = UUID.fromString("019c0000-0000-7000-8000-000000000001");

        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {


                Future<?> first = executor.submit(() -> {
                    try {
                        execute(start, key, request);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                Future<?> second = executor.submit(() -> {
                    try {
                        execute(start, key, request);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                start.countDown();
                first.get();
                second.get();



        }



        Wallet sender = walletRepository.findById(wallets.senderId()).orElseThrow();
        Wallet receiver = walletRepository.findById(wallets.receiverId()).orElseThrow();

        assertEquals(7_500L, sender.getBalancePaise());
        assertEquals(2_500L, receiver.getBalancePaise());
        assertEquals(1L, transferRepository.count());
        assertEquals(2L, ledgerTransactionRepository.count());
    }

    private void execute(CountDownLatch start, UUID key, TransferRequest request)
        throws InterruptedException {
        start.await();
        transferService.createTransfer(key, request);
    }

    private TestWallets createWallets() {
        return transactionTemplate.execute(status -> {
            Wallet sender = walletRepository.save(new Wallet(10_000L, WalletStatus.ACTIVE));
            Wallet receiver = walletRepository.save(new Wallet(0L, WalletStatus.ACTIVE));
            walletRepository.flush();
            return new TestWallets(sender.getId(), receiver.getId());
        });
    }

    private record TestWallets(UUID senderId, UUID receiverId) {}
}
