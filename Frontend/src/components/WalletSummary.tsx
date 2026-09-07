import type { WalletResponse } from "../types/wallet";
import { formatPaise } from "../utils/money";
export function WalletSummary({ wallet }: { wallet: WalletResponse }) { return <section className="card wallet-summary"><div><span className="eyebrow">Wallet balance</span><h2>{formatPaise(wallet.balancePaise)}</h2></div><div className="wallet-meta"><span className={`status status-${wallet.status.toLowerCase()}`}>{wallet.status}</span><code>{wallet.id}</code></div></section>; }
