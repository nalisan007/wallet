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
    @Query("""
        DELETE FROM IdempotencyRecord r
        WHERE r.createdAt < :cutoff
        """)
    int deleteExpired(
        @Param("cutoff") Instant cutoff
    );
}
