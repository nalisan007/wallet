package io.wallet.service;

import io.wallet.entity.TransferRequest;
import io.wallet.entity.Wallet;
import io.wallet.entity.WalletStatus;
import io.wallet.repository.LedgerTransactionRepository;
import io.wallet.repository.TransferRepository;
import io.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class IdempotencyConcurrencyIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private LedgerTransactionRepository ledgerTransactionRepository;

    @Autowired
    private TransferService transferService;

    @Test
    void sameIdempotencyKeyMustMoveMoneyExactlyOnce()
        throws Exception {

        TestWallets wallets = createWallets();

        long initialSenderBalance = 10_000L;
        long initialReceiverBalance = 0L;
        long transferAmount = 2_500L;

        TransferRequest request = new TransferRequest(
            wallets.senderId(),
            wallets.receiverId(),
            transferAmount
        );

        UUID idempotencyKey =
            UUID.fromString(
                "019c0000-0000-7000-8000-000000000001"
            );

        CountDownLatch startLatch = new CountDownLatch(1);

        try (ExecutorService executor =
                 Executors.newFixedThreadPool(2)) {

            Future<?> first =
                executor.submit(() -> {
                    await(startLatch);

                    transferService.createTransfer(
                        idempotencyKey,
                        request
                    );

                    return null;
                });

            Future<?> second =
                executor.submit(() -> {
                    await(startLatch);

                    transferService.createTransfer(
                        idempotencyKey,
                        request
                    );

                    return null;
                });

            startLatch.countDown();

            first.get();
            second.get();
        }

        Wallet sender = walletRepository
            .findById(wallets.senderId())
            .orElseThrow();

        Wallet receiver = walletRepository
            .findById(wallets.receiverId())
            .orElseThrow();

        assertEquals(
            initialSenderBalance - transferAmount,
            sender.getBalancePaise()
        );

        assertEquals(
            initialReceiverBalance + transferAmount,
            receiver.getBalancePaise()
        );

        assertEquals(
            1,
            transferRepository.count()
        );

        assertEquals(
            2,
            ledgerTransactionRepository.count()
        );

        assertNotNull(
            transferRepository.findAll().getFirst()
        );
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                "Test thread was interrupted",
                exception
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected TestWallets createWallets() {

        Wallet sender = new Wallet(
            10_000L,
            WalletStatus.ACTIVE
        );

        Wallet receiver = new Wallet(
            0L,
            WalletStatus.ACTIVE
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        walletRepository.flush();

        return new TestWallets(
            sender.getId(),
            receiver.getId()
        );
    }

    private record TestWallets(
        UUID senderId,
        UUID receiverId
    ) {
    }
}
