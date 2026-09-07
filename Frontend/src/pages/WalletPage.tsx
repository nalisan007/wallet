import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getWallet } from "../api/walletApi";
import type { WalletResponse } from "../types/wallet";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { LoadingState } from "../components/LoadingState";
import { WalletSummary } from "../components/WalletSummary";
import { formatPaise } from "../utils/money";

export function WalletPage() {
  const { walletId } = useParams();
  const [wallet, setWallet] = useState<WalletResponse | null>(null);
  const [error, setError] = useState<unknown>(null);

  const load = useCallback(async () => {
    if (!walletId) return;
    try { setError(null); setWallet(await getWallet(walletId)); }
    catch (e) { setError(e); }
  }, [walletId]);

  useEffect(() => { void load(); }, [load]);

  if (!walletId) return <ApiErrorMessage error={new Error("Wallet ID is required.")} />;
  if (!wallet && !error) return <LoadingState label="Loading wallet..." />;
  if (error) return <ApiErrorMessage error={error} onRetry={() => void load()} />;
  if (!wallet) return null;

  return <>
    <WalletSummary wallet={wallet} />
    <section className="card">
      <div className="section-heading"><div><span className="eyebrow">Recent activity</span><h2>Latest ledger activity</h2></div></div>
      {wallet.recentActivity.length === 0 ? <p className="muted">No activity yet.</p> : <div className="activity-list">{wallet.recentActivity.map(a => <div className="activity-row" key={a.ledgerTransactionId}><span>{a.transactionType} · {a.entryType}</span><strong>{a.entryType === "CREDIT" ? "+" : "-"}{formatPaise(a.amountPaise)}</strong></div>)}</div>}
    </section>
    <nav className="action-grid">
      <Link className="action-card" to={`/api/v1/wallets/${walletId}/transfer`}><strong>New transfer</strong><span>Send money to another wallet</span></Link>
      <Link className="action-card" to={`/api/v1/wallets/${walletId}/transfers`}><strong>Transfer history</strong><span>Review wallet transfers</span></Link>
      <Link className="action-card" to={`/api/v1/wallets/${walletId}/statement`}><strong>Statement</strong><span>View ledger activity</span></Link>
    </nav>
  </>;
}
