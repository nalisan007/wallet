import { apiClient } from "./client";
import type { TransferHistoryParams, TransferHistoryResponse, TransferRequest, TransferResponse } from "../types/transfer";

export function createTransfer(walletId: string, request: TransferRequest, idempotencyKey: string): Promise<TransferResponse> {
  return apiClient.post<TransferResponse>(
    `/wallets/${encodeURIComponent(walletId)}/transfers`,
    request,
    { "Idempotency-Key": idempotencyKey }
  );
}

export function getTransferHistory(walletId: string, params: TransferHistoryParams): Promise<TransferHistoryResponse> {
  const search = new URLSearchParams({ walletId });
  if (params.from) search.set("from", params.from);
  if (params.to) search.set("to", params.to);
  if (params.cursor) search.set("cursor", params.cursor);
  search.set("limit", String(params.limit ?? 20));
  return apiClient.get<TransferHistoryResponse>(`/transfers?${search.toString()}`);
}
