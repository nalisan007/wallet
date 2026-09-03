package io.wallet.repository;

import io.wallet.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerTransactionRepository
    extends JpaRepository<LedgerTransaction, UUID> {

    @Query("""
        SELECT l
        FROM LedgerTransaction l
        WHERE l.walletId = :walletId
          AND l.createdAt >= :from
          AND l.createdAt <= :to
        ORDER BY l.createdAt ASC, l.id ASC
        """)
    List<LedgerTransaction> findStatementEntries(
        @Param("walletId") UUID walletId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("""
        SELECT COALESCE(
            SUM(
                CASE
                    WHEN l.entryType =
                        io.wallet.entity.LedgerEntryType.CREDIT
                    THEN l.amountPaise
                    ELSE -l.amountPaise
                END
            ),
            0
        )
        FROM LedgerTransaction l
        WHERE l.walletId = :walletId
          AND l.createdAt < :from
        """)
    Long calculateOpeningBalancePaise(
        @Param("walletId") UUID walletId,
        @Param("from") Instant from
    );

    List<LedgerTransaction> findByTransferIdOrderByIdAsc(
        UUID transferId
    );
}
