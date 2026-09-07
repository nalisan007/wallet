import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getWallet } from "../api/walletApi";
import type { WalletResponse } from "../types/wallet";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { LoadingState } from "../components/LoadingState";
import { TransferForm } from "../components/TransferForm";

export function TransferPage() {
  const { walletId } = useParams();
  const [wallet, setWallet] = useState<WalletResponse | null>(null);
  const [error, setError] = useState<unknown>(null);
  const load = useCallback(async () => { if (!walletId) return; try { setError(null); setWallet(await getWallet(walletId)); } catch (e) { setError(e); } }, [walletId]);
  useEffect(() => { void load(); }, [load]);
  if (!walletId) return <ApiErrorMessage error={new Error("Wallet ID is required.")} />;
  if (error) return <ApiErrorMessage error={error} onRetry={() => void load()} />;
  if (!wallet) return <LoadingState label="Loading wallet..." />;
  return <><Link className="back-link" to={`/api/v1/wallets/${walletId}`}>← Wallet</Link><TransferForm wallet={wallet} onSuccess={() => void load()} /></>;
}
