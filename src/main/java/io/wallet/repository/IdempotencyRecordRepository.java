package io.wallet.repository;

import io.wallet.entity.IdempotencyRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepository
    extends JpaRepository<IdempotencyRecord, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM IdempotencyRecord r
        WHERE r.idempotencyKey = :idempotencyKey
        """)
    Optional<IdempotencyRecord> findByIdempotencyKeyForUpdate(
        @Param("idempotencyKey") UUID idempotencyKey
    );

    @Modifying
    @Query("""
        DELETE FROM IdempotencyRecord r
        WHERE r.createdAt < :cutoff
        AND r.transferId IS NOT NULL
        """)
    int deleteExpiredCompletedRecords(
        @Param("cutoff") Instant cutoff
    );
}
