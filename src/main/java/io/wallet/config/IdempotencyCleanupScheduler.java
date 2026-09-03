package io.wallet.config;

import io.wallet.service.IdempotencyCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyCleanupScheduler {

    private static final Logger log =
        LoggerFactory.getLogger(IdempotencyCleanupScheduler.class);

    private final IdempotencyCleanupService cleanupService;

    public IdempotencyCleanupScheduler(IdempotencyCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(fixedDelayString = "${wallet.idempotency.cleanup-delay-ms:3600000}")
    public void cleanupExpiredIdempotencyRecords() {
        long deleted = cleanupService.deleteExpiredRecords();
        log.info("Deleted {} expired idempotency records", deleted);
    }
}
