import { useCallback, useEffect, useState } from "react";
import { getTransferHistory } from "../api/transferApi";
import type { TransferHistoryResponse } from "../types/transfer";
import { formatDateTime } from "../utils/date";
import { formatPaise } from "../utils/money";
import { ApiErrorMessage } from "./ApiErrorMessage";
import { EmptyState } from "./EmptyState";
import { LoadingState } from "./LoadingState";

export function TransferHistory({
  walletId,
  from,
  to
}: {
  walletId: string;
  from?: string;
  to?: string;
}) {
  const [pages, setPages] = useState<TransferHistoryResponse[]>([]);
  const [cursor, setCursor] = useState<string | undefined>();
  const [loading, setLoading] = useState(true);
  const [loadingNext, setLoadingNext] = useState(false);
  const [error, setError] = useState<unknown>(null);

  const load = useCallback(async (nextCursor?: string) => {
    if (nextCursor) setLoadingNext(true);
    else setLoading(true);

    setError(null);

    try {
      const result = await getTransferHistory(walletId, {
        from,
        to,
        cursor: nextCursor,
        limit: 20
      });

      if (nextCursor) {
        setPages((previous) => [...previous, result]);
      } else {
        setPages([result]);
      }

      setCursor(result.nextCursor || undefined);
    } catch (requestError) {
      setError(requestError);
    } finally {
      setLoading(false);
      setLoadingNext(false);
    }
  }, [walletId, from, to]);

  useEffect(() => {
    setPages([]);
    setCursor(undefined);
    void load();
  }, [load]);

  if (loading) return <LoadingState label="Loading transfer history..." />;
  if (error) {
    return (
      <ApiErrorMessage
        error={error}
        onRetry={() => void load()}
      />
    );
  }

  const transfers = pages.flatMap((page) => page.transfers);

  return (
    <section className="card">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Activity</span>
          <h2>Transfer history</h2>
        </div>
      </div>

      {transfers.length === 0 ? (
        <EmptyState message="No transfers found for this period." />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Direction</th>
                <th>Counterparty</th>
                <th>Amount</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {transfers.map((transfer) => {
                const sent = transfer.fromWalletId === walletId;
                return (
                  <tr key={transfer.id}>
                    <td>{formatDateTime(transfer.createdAt)}</td>
                    <td>
                      <span className={`direction ${sent ? "debit" : "credit"}`}>
                        {sent ? "Sent" : "Received"}
                      </span>
                    </td>
                    <td>
                      <code>
                        {sent ? transfer.toWalletId : transfer.fromWalletId}
                      </code>
                    </td>
                    <td className={sent ? "money debit" : "money credit"}>
                      {sent ? "-" : "+"}{formatPaise(transfer.amountPaise)}
                    </td>
                    <td>{transfer.status}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {cursor && (
        <div className="pagination">
          <button
            className="secondary-button"
            disabled={loadingNext}
            onClick={() => void load(cursor)}
          >
            {loadingNext ? "Loading..." : "Load next page"}
          </button>
        </div>
      )}
    </section>
  );
}