package io.wallet.service;

import io.wallet.repository.IdempotencyRecordRepository;
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
        Duration retention
    ) {
        if (retention == null || retention.isNegative() || retention.isZero()) {
            throw new IllegalArgumentException("Idempotency retention must be positive");
        }
        this.repository = repository;
        this.clock = clock;
        this.retention = retention;
    }

    @Transactional
    public long deleteExpiredRecords() {
        Instant cutoff = clock.instant().minus(retention);
        return repository.deleteExpiredCompletedRecords(cutoff);
    }

    public long cleanup() {
        return deleteExpiredRecords();
    }
}
