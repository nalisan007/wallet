package io.wallet.repository;

import io.wallet.entity.LedgerEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("""
        SELECT e
        FROM LedgerEntry e
        WHERE e.walletId = :walletId
          AND (:fromDate IS NULL OR e.createdAt >= :fromDate)
          AND (:toDate IS NULL OR e.createdAt <= :toDate)
          AND (
              :cursorCreatedAt IS NULL
              OR e.createdAt < :cursorCreatedAt
              OR (
                  e.createdAt = :cursorCreatedAt
                  AND e.id < :cursorId
              )
          )
        ORDER BY e.createdAt DESC, e.id DESC
        """)
    Slice<LedgerEntry> findWalletEntries(
        @Param("walletId") UUID walletId,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("cursorCreatedAt") Instant cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );
}
