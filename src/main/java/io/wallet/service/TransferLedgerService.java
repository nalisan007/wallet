package io.wallet.service;

import io.wallet.entity.LedgerEntryType;
import io.wallet.entity.LedgerTransaction;
import io.wallet.entity.Transfer;
import io.wallet.entity.Wallet;
import io.wallet.repository.LedgerTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransferLedgerService {

    private final LedgerTransactionRepository ledgerRepository;

    public TransferLedgerService(
        LedgerTransactionRepository ledgerRepository
    ) {
        this.ledgerRepository = ledgerRepository;
    }

    public Transfer createTransfer(
        Wallet fromWallet,
        Wallet toWallet,
        long amountPaise
    ) {
        fromWallet.debit(amountPaise);
        toWallet.credit(amountPaise);

        return new Transfer(
            fromWallet.getId(),
            toWallet.getId(),
            amountPaise
        );
    }

    public void createLedgerEntries(
        Transfer transfer
    ) {
        LedgerTransaction debit =
            new LedgerTransaction(
                transfer.getFromWalletId(),
                transfer.getId(),
                LedgerEntryType.DEBIT,
                transfer.getAmountPaise()
            );

        LedgerTransaction credit =
            new LedgerTransaction(
                transfer.getToWalletId(),
                transfer.getId(),
                LedgerEntryType.CREDIT,
                transfer.getAmountPaise()
            );

        ledgerRepository.save(debit);
        ledgerRepository.save(credit);
    }
}
