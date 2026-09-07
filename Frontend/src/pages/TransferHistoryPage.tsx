import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { DateRangeFilter } from "../components/DateRangeFilter";
import { TransferHistory } from "../components/TransferHistory";
import { ApiErrorMessage } from "../components/ApiErrorMessage";

export function TransferHistoryPage() {
  const { walletId } = useParams();
  const [range, setRange] = useState<{ from?: string; to?: string }>({});
  if (!walletId) return <ApiErrorMessage error={new Error("Wallet ID is required.")} />;
  return <><Link className="back-link" to={`/api/v1/wallets/${walletId}`}>← Wallet</Link><div className="page-heading"><span className="eyebrow">Activity</span><h1>Transfer history</h1></div><DateRangeFilter onApply={(from, to) => setRange({ from, to })} /><TransferHistory walletId={walletId} from={range.from} to={range.to} /></>;
}
