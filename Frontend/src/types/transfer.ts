export interface TransferRequest {
  fromWalletId: string;
  toWalletId: string;
  amountPaise: number;
}

export interface TransferResponse {
  id: string;
  fromWalletId: string;
  toWalletId: string;
  amountPaise: number;
  status: string;
  createdAt: string;
}

export interface TransferHistoryResponse {
  transfers: TransferResponse[];
  nextCursor: string | null;
  hasNext: boolean;
}

export interface TransferHistoryParams {
  from?: string;
  to?: string;
  cursor?: string;
  limit?: number;
}