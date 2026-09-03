package io.wallet.service;

import io.wallet.repository.IdempotencyRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class IdempotencyCleanupService {

    private final IdempotencyRecordRepository repository;
    private final Clock clock;
    private final Duration retention;

    public IdempotencyCleanupService(
        IdempotencyRecordRepository repository,
        Clock clock,
        @Value("${wallet.idempotency.retention}") Duration retention
    ) {
        this.repository = repository;
        this.clock = clock;
        this.retention = retention;
    }

    @Scheduled(
        fixedDelayString = "${wallet.idempotency.cleanup-delay}"
    )
    @Transactional
    public int cleanup() {
        Instant cutoff =
            Instant.now(clock).minus(retention);

        return repository.deleteExpiredCompletedRecords(
            cutoff
        );
    }
}
