package io.wallet.repository;

import io.wallet.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface IdempotencyRecordRepository
    extends JpaRepository<IdempotencyRecord, UUID> {

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO idempotency_records
            (idempotency_key, request_hash, created_at)
        VALUES
            (:key, :requestHash, :createdAt)
        """, nativeQuery = true)
    int insertIfAbsent(
        @Param("key") UUID key,
        @Param("requestHash") String requestHash,
        @Param("createdAt") Instant createdAt
    );

    @Modifying
    @Query("""
        DELETE FROM IdempotencyRecord r
        WHERE r.createdAt < :cutoff
          AND r.transferId IS NOT NULL
        """)
    int deleteExpiredCompletedRecords(@Param("cutoff") Instant cutoff);
}
