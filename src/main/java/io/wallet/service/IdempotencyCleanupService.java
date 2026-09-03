package io.wallet.service;

import io.wallet.repository.IdempotencyRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class IdempotencyCleanupService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final long retentionDays;

    public IdempotencyCleanupService(
        IdempotencyRecordRepository idempotencyRecordRepository,
        @Value("${wallet.idempotency.retention-days}")
        long retentionDays
    ) {
        if (retentionDays <= 0) {
            throw new IllegalArgumentException(
                "Idempotency retention days must be greater than zero"
            );
        }

        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(
        fixedDelayString = "${wallet.idempotency.cleanup-delay-ms}"
    )
    @Transactional
    public void deleteExpiredRecords() {

        Instant cutoff = Instant.now()
            .minus(retentionDays, ChronoUnit.DAYS);

        idempotencyRecordRepository
            .deleteByCreatedAtBefore(cutoff);
    }
}
