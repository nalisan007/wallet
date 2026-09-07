import { FormEvent, useState } from "react";
import { v7 as uuidv7 } from "uuid";
import { createTransfer } from "../api/transferApi";
import type { WalletResponse } from "../types/wallet";
import { formatPaise, parseRupeesToPaise } from "../utils/money";
import { validateTransfer } from "../utils/validation";
import { ApiErrorMessage } from "./ApiErrorMessage";

export function TransferForm({ wallet, onSuccess }: { wallet: WalletResponse; onSuccess: () => void }) {
  const [toWalletId, setToWalletId] = useState("");
  const [amountRupees, setAmountRupees] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSuccess(false);

    const amountPaise = parseRupeesToPaise(amountRupees);
    const validationError = validateTransfer(wallet.id, toWalletId, amountPaise ?? 0);

    if (validationError || amountPaise === null) {
      setError(new Error(validationError || "Enter a valid rupee amount with up to two decimal places."));
      return;
    }

    if (amountPaise > wallet.balancePaise) {
      setError(new Error(`Amount exceeds available balance of ${formatPaise(wallet.balancePaise)}.`));
      return;
    }

    setSubmitting(true);
    try {
      await createTransfer(
        wallet.id,
        { fromWalletId: wallet.id, toWalletId: toWalletId.trim(), amountPaise },
        uuidv7()
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
      <div className="section-heading"><div><span className="eyebrow">Move money</span><h2>New transfer</h2></div></div>
      <form className="form-grid" onSubmit={submit}>
        <label>From wallet<input value={wallet.id} readOnly /></label>
        <label>To wallet<input value={toWalletId} onChange={e => setToWalletId(e.target.value)} placeholder="Destination wallet UUID" disabled={submitting} /></label>
        <label>Amount (₹)<input value={amountRupees} onChange={e => setAmountRupees(e.target.value)} inputMode="decimal" placeholder="100.00" disabled={submitting} /></label>
        <p className="hint">Current balance: {formatPaise(wallet.balancePaise)}</p>
        {error && <ApiErrorMessage error={error} />}
        {success && <div className="success-card" role="status">Transfer completed successfully.</div>}
        <button className="primary-button" disabled={submitting} type="submit">{submitting ? "Transferring..." : "Transfer"}</button>
      </form>
    </section>
  );
}
