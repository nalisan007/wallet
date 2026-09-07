import type { ApiException } from "../types/api";

const friendly: Record<string, string> = {
  INSUFFICIENT_BALANCE: "Insufficient wallet balance.",
  SELF_TRANSFER: "Source and destination wallets must be different.",
  WALLET_NOT_FOUND: "Wallet was not found.",
  WALLET_INACTIVE: "This wallet is inactive.",
  IDEMPOTENCY_KEY_REUSE: "This idempotency key was already used for a different request.",
  IDEMPOTENCY_REQUEST_PROCESSING: "This transfer request is already being processed.",
  INVALID_IDEMPOTENCY_KEY: "The idempotency key is invalid.",
  INVALID_CURSOR: "The history cursor is invalid. Reload the history.",
  INVALID_DATE_RANGE: "The end date must be greater than or equal to the start date.",
  VALIDATION_ERROR: "Please correct the highlighted request values."
};

export function getErrorMessage(error: unknown): string {
  if (error && typeof error === "object" && "code" in error) {
    const apiError = error as ApiException;
    return friendly[apiError.code] || apiError.message;
  }
  return error instanceof Error ? error.message : "An unexpected error occurred.";
}

export function ApiErrorMessage({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <div className="error-card" role="alert">
      <span>{getErrorMessage(error)}</span>
      {onRetry && <button onClick={onRetry}>Retry</button>}
    </div>
  );
}
