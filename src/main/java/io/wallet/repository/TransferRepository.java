package io.wallet.repository;

import io.wallet.entity.Transfer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    @Query("""
        SELECT t
        FROM Transfer t
        WHERE (t.fromWalletId = :walletId OR t.toWalletId = :walletId)
          AND (:fromDate IS NULL OR t.createdAt >= :fromDate)
          AND (:toDate IS NULL OR t.createdAt <= :toDate)
          AND (
              :cursorCreatedAt IS NULL
              OR t.createdAt < :cursorCreatedAt
              OR (
                  t.createdAt = :cursorCreatedAt
                  AND t.id < :cursorId
              )
          )
        ORDER BY t.createdAt DESC, t.id DESC
        """)
    List<Transfer> findWalletTransfers(
        @Param("walletId") UUID walletId,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("cursorCreatedAt") Instant cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );
}
