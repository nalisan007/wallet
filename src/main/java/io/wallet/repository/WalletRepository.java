package io.wallet.repository;

import io.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Loads a wallet while acquiring a database row-level pessimistic write lock.
     *
     * The lock is held until the surrounding transaction completes.
     * This method must only be called from a transactional service operation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT w
        FROM Wallet w
        WHERE w.id = :walletId
        """)
    Optional<Wallet> findByIdForUpdate(
        @Param("walletId") UUID walletId
    );

    /**
     * Finds the active system wallet.
     */
    Optional<Wallet> findByWalletTypeAndStatus(
        io.wallet.entity.WalletType walletType,
        io.wallet.entity.WalletStatus status
    );
}
