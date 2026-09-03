export function validateTransfer(
  sourceWalletId: string,
  destinationWalletId: string,
  amountPaise: number
): string | null {
  if (!destinationWalletId.trim()) {
    return "Destination wallet is required.";
  }

  if (sourceWalletId === destinationWalletId.trim()) {
    return "Source and destination wallets must be different.";
  }

  if (!Number.isSafeInteger(amountPaise) || amountPaise <= 0) {
    return "Amount must be a positive whole number of paise.";
  }

  return null;
}

export function validateDateRange(from: string, to: string): string | null {
  if (!from || !to) {
    return "Both start and end dates are required.";
  }

  if (new Date(from).getTime() > new Date(to).getTime()) {
    return "The start date must be before the end date.";
  }

  return null;
}