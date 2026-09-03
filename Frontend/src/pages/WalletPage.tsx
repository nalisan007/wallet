import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getWallet } from "../api/walletApi";
import type { WalletResponse } from "../types/wallet";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { LoadingState } from "../components/LoadingState";
import { WalletSummary } from "../components/WalletSummary";

export function WalletPage() {
  const { walletId } = useParams();
  const [wallet, setWallet] = useState<WalletResponse | null>(null);
  const [error, setError] = useState<unknown>(null);

  const load = useCallback(async () => {
    if (!walletId) return;
    setError(null);
    try {
      setWallet(await getWallet(walletId));
    } catch (requestError) {
      setError(requestError);
    }
  }, [walletId]);

  useEffect(() => {
    void load();
  }, [load]);

  if (!walletId) return <ApiErrorMessage error={new Error("Wallet ID is required.")} />;
  if (!wallet && !error) return <LoadingState label="Loading wallet..." />;
  if (error) return <ApiErrorMessage error={error} onRetry={() => void load()} />;
  if (!wallet) return null;

  return (
    <>
      <WalletSummary wallet={wallet} />
      <nav className="action-grid">
        <Link className="action-card" to={`/wallets/${walletId}/transfer`}>
          <strong>New transfer</strong>
          <span>Send money to another wallet</span>
        </Link>
        <Link className="action-card" to={`/wallets/${walletId}/transfers`}>
          <strong>Transfer history</strong>
          <span>Review wallet transfers</span>
        </Link>
        <Link className="action-card" to={`/wallets/${walletId}/statement`}>
          <strong>Statement</strong>
          <span>View ledger activity and balances</span>
        </Link>
      </nav>
    </>
  );
}