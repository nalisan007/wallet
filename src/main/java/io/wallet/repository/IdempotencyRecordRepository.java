package io.wallet.repository;

import io.wallet.entity.IdempotencyRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, UUID> {

    Optional<IdempotencyRecord> findByIdempotencyKey(
        UUID idempotencyKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i
        FROM IdempotencyRecord i
        WHERE i.idempotencyKey = :idempotencyKey
        """)
    Optional<IdempotencyRecord> findByIdempotencyKeyForUpdate(
        @Param("idempotencyKey") UUID idempotencyKey
    );

    long deleteByCreatedAtBefore(Instant cutoff);
}
