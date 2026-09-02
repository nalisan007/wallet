package io.wallet.repository;

import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.LedgerTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerTransactionRepository
        extends JpaRepository<LedgerTransaction, UUID> {

    Optional<LedgerTransaction> findByTransactionTypeAndReferenceId(
        LedgerTransactionType transactionType,
        UUID referenceId
    );
}
