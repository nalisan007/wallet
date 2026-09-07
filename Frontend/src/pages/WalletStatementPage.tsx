import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getWalletStatement } from "../api/statementApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DateRangeFilter } from "../components/DateRangeFilter";
import { LoadingState } from "../components/LoadingState";
import { StatementTable } from "../components/StatementTable";
import type { WalletStatementResponse } from "../types/statement";

export function WalletStatementPage() {
  const { walletId } = useParams();
  const [statement, setStatement] = useState<WalletStatementResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  if (!walletId) return <ApiErrorMessage error={new Error("Wallet ID is required.")} />;
  async function load(from: string, to: string) { setLoading(true); setError(null); try { setStatement(await getWalletStatement(walletId, from, to)); } catch (e) { setError(e); } finally { setLoading(false); } }
  return <><Link className="back-link" to={`/api/v1/wallets/${walletId}`}>← Wallet</Link><div className="page-heading"><span className="eyebrow">Ledger</span><h1>Wallet statement</h1></div><DateRangeFilter onApply={(from, to) => void load(from, to)} />{loading && <LoadingState label="Loading statement..." />}{error && <ApiErrorMessage error={error} />}{statement && !loading && <StatementTable statement={statement} />}{!statement && !loading && !error && <div className="state-card">Select a date range to load the statement.</div>}</>;
}
