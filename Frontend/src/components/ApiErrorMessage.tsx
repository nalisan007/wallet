import type { ApiException } from "../types/api";

const messages: Record<string, string> = {
  INSUFFICIENT_BALANCE: "Insufficient wallet balance.",
  SELF_TRANSFER: "Source and destination wallets must be different.",
  WALLET_NOT_FOUND: "Wallet was not found.",
  WALLET_INACTIVE: "This wallet is inactive.",
  IDEMPOTENCY_KEY_REUSE: "This request key was already used for a different transfer.",
  INVALID_IDEMPOTENCY_KEY: "The transfer request key is invalid.",
  INVALID_CURSOR: "This history cursor is no longer valid. Reload the history.",
  INVALID_DATE_RANGE: "The selected date range is invalid.",
  VALIDATION_ERROR: "Please correct the submitted values."
};

export function getErrorMessage(error: unknown): string {
  if (error && typeof error === "object" && "code" in error) {
    const apiError = error as ApiException;
    return messages[apiError.code] || apiError.message;
  }
  return error instanceof Error ? error.message : "An unexpected error occurred.";
}

export function ApiErrorMessage({
  error,
  onRetry
}: {
  error: unknown;
  onRetry?: () => void;
}) {
  return (
    <div className="error-card" role="alert">
      <span>{getErrorMessage(error)}</span>
      {onRetry && <button onClick={onRetry}>Retry</button>}
    </div>
  );
}