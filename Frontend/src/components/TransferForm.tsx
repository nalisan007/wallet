import { FormEvent, useState } from "react";
import { createTransfer } from "../api/transferApi";
import type { WalletResponse } from "../types/wallet";
import { formatPaise } from "../utils/money";
import { validateTransfer } from "../utils/validation";
import { ApiErrorMessage } from "./ApiErrorMessage";

export function TransferForm({
  wallet,
  onSuccess
}: {
  wallet: WalletResponse;
  onSuccess: () => void;
}) {
  const [toWalletId, setToWalletId] = useState("");
  const [amountRupees, setAmountRupees] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSuccess(false);

    const normalizedAmount = amountRupees.trim();
    const amountPaise = /^\d+(?:\.\d{1,2})?$/.test(normalizedAmount)
      ? Math.round(Number(normalizedAmount) * 100)
      : Number.NaN;

    const validationError = validateTransfer(
      wallet.id,
      toWalletId,
      amountPaise
    );

    if (validationError) {
      setError(new Error(validationError));
      return;
    }

    if (amountPaise > wallet.balancePaise) {
      setError(new Error(
        `Amount exceeds available balance of ${formatPaise(wallet.balancePaise)}.`
      ));
      return;
    }

    setSubmitting(true);

    try {
      const idempotencyKey = crypto.randomUUID();

      await createTransfer(
        wallet.id,
        {
          fromWalletId: wallet.id,
          toWalletId: toWalletId.trim(),
          amountPaise
        },
        idempotencyKey
      );

      setSuccess(true);
      setToWalletId("");
      setAmountRupees("");
      onSuccess();
    } catch (requestError) {
      setError(requestError);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="card">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Move money</span>
          <h2>New transfer</h2>
        </div>
      </div>

      <form className="form-grid" onSubmit={submit}>
        <label>
          From wallet
          <input value={wallet.id} readOnly />
        </label>

        <label>
          To wallet
          <input
            value={toWalletId}
            onChange={(event) => setToWalletId(event.target.value)}
            placeholder="Destination wallet UUID"
            disabled={submitting}
          />
        </label>

        <label>
          Amount (₹)
          <input
            value={amountRupees}
            onChange={(event) => setAmountRupees(event.target.value)}
            inputMode="decimal"
            placeholder="100.00"
            disabled={submitting}
          />
        </label>

        {error && <ApiErrorMessage error={error} />}
        {success && (
          <div className="success-card" role="status">
            Transfer completed successfully.
          </div>
        )}

        <button className="primary-button" disabled={submitting} type="submit">
          {submitting ? "Transferring..." : "Transfer"}
        </button>
      </form>
    </section>
  );
}