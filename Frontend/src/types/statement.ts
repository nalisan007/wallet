export type LedgerEntryType = "DEBIT" | "CREDIT";
export type LedgerTransactionType = "TRANSFER" | "DEPOSIT" | "WITHDRAWAL";

export interface WalletActivityResponse {
  ledgerTransactionId: string;
  transferId: string | null;
  transactionType: LedgerTransactionType;
  entryType: LedgerEntryType;
  amountPaise: number;
  createdAt: string;
}

export interface StatementEntryResponse {
  ledgerTransactionId: string;
  transferId: string | null;
  transactionType: LedgerTransactionType;
  entryType: LedgerEntryType;
  amountPaise: number;
  balanceAfterPaise: number;
  createdAt: string;
}

export interface WalletStatementResponse {
  walletId: string;
  from: string;
  to: string;
  openingBalancePaise: number;
  entries: StatementEntryResponse[];
  closingBalancePaise: number;
}
