package io.wallet.service;

import io.wallet.repository.IdempotencyRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class IdempotencyCleanupService {

    private final IdempotencyRecordRepository repository;
    private final long retentionDays;

    public IdempotencyCleanupService(
        IdempotencyRecordRepository repository,
        @Value("${wallet.idempotency.retention-days:7}")
        long retentionDays
    ) {
        this.repository = repository;
        this.retentionDays = retentionDays;
    }

    @Transactional
    public long deleteExpiredRecords() {
        Instant cutoff =
            Instant.now().minus(
                retentionDays,
                ChronoUnit.DAYS
            );

        return repository.deleteByCreatedAtBefore(cutoff);
    }
}
