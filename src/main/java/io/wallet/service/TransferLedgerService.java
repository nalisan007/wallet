package io.wallet.service;

import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.Transfer;
import io.wallet.repository.LedgerTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransferLedgerService {

    private final LedgerTransactionRepository ledgerTransactionRepository;

    public TransferLedgerService(
        LedgerTransactionRepository ledgerTransactionRepository
    ) {
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    public void createLedgerEntries(Transfer transfer) {
        LedgerTransaction debit = new LedgerTransaction(
            transfer.getFromWalletId(),
            transfer.getId(),
            LedgerEntryType.DEBIT,
            transfer.getAmountPaise()
        );

        LedgerTransaction credit = new LedgerTransaction(
            transfer.getToWalletId(),
            transfer.getId(),
            LedgerEntryType.CREDIT,
            transfer.getAmountPaise()
        );

        ledgerTransactionRepository.save(debit);
        ledgerTransactionRepository.save(credit);
    }
}
