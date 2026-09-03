import { apiClient } from "./client";
import type {
  TransferHistoryParams,
  TransferHistoryResponse,
  TransferRequest,
  TransferResponse
} from "../types/transfer";

export function createTransfer(
  walletId: string,
  request: TransferRequest,
  idempotencyKey: string
): Promise<TransferResponse> {
  return apiClient.post<TransferResponse>(
    `/api/v1/wallets/${encodeURIComponent(walletId)}/transfers`,
    request,
    { "Idempotency-Key": idempotencyKey }
  );
}

export function getTransferHistory(
  walletId: string,
  params: TransferHistoryParams
): Promise<TransferHistoryResponse> {
  const search = new URLSearchParams();

  if (params.from) search.set("from", params.from);
  if (params.to) search.set("to", params.to);
  if (params.cursor) search.set("cursor", params.cursor);
  if (params.limit !== undefined) search.set("limit", String(params.limit));

  const query = search.toString();
  return apiClient.get<TransferHistoryResponse>(
    `/api/v1/wallets/${encodeURIComponent(walletId)}/transfers${query ? `?${query}` : ""}`
  );
}