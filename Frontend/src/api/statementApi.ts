import { apiClient } from "./client";
import type { WalletStatementResponse } from "../types/statement";

export function getWalletStatement(walletId: string, from: string, to: string): Promise<WalletStatementResponse> {
  const params = new URLSearchParams({ from, to });
  return apiClient.get<WalletStatementResponse>(
    `/wallets/${encodeURIComponent(walletId)}/statement?${params.toString()}`
  );
}
