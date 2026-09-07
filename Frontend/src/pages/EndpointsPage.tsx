import { Link } from "react-router-dom";

const userWallet = "01999000-0000-7000-8000-000000000004";

export function EndpointsPage() {
  return (
    <section>
      <div className="page-heading"><span className="eyebrow">MVP API</span><h1>Wallet Transfer</h1><p>Development endpoint index.</p></div>
      <div className="card endpoint-list">
        <div><b>GET</b><code>/api/v1/wallets/&#123;id&#125;</code><span>Wallet balance and recent activity</span></div>
        <div><b>POST</b><code>/api/v1/wallets/&#123;walletId&#125;/transfers</code><span>Create an idempotent transfer</span></div>
        <div><b>GET</b><code>/api/v1/transfers?walletId=&amp;from=&amp;to=&amp;limit=20&amp;cursor=</code><span>Cursor-paginated transfer history</span></div>
        <div><b>GET</b><code>/api/v1/wallets/&#123;id&#125;/statement?from=&amp;to=</code><span>Ledger-derived wallet statement</span></div>
      </div>
      <div className="card">
        <h2>Seeded user wallet</h2>
        <code>{userWallet}</code>
        <div className="action-grid single-row">
        <Link
        className="action-card"
        to={`/api/v1/wallets/${userWallet}`}
        >
        <strong>Open wallet</strong>
        <span>Test the seeded ₹5,000 wallet</span>
        </Link>

        <Link
        className="action-card"
        to={`/api/v1/wallets/${userWallet}/transfer`}
        >
        <strong>Transfer</strong>
        <span>Send funds to another wallet</span>
        </Link>

        <Link
        className="action-card"
        to={`/api/v1/wallets/${userWallet}/transfers`}
        >
        <strong>Transfer History</strong>
        <span>View cursor-paginated transfer history</span>
        </Link>

        <Link
        className="action-card"
        to={`/api/v1/wallets/${userWallet}/statement`}
        >
        <strong>Statement</strong>
        <span>View ledger-derived balance</span>
        </Link>
        </div>
      </div>
    </section>
  );
}
