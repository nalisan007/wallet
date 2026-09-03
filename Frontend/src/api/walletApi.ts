import { apiClient } from "./client";
import type { WalletResponse } from "../types/wallet";

export function getWallet(walletId: string): Promise<WalletResponse> {
  return apiClient.get<WalletResponse>(
    `/api/v1/wallets/${encodeURIComponent(walletId)}`
  );
}