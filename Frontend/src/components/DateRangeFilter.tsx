import { useState } from "react";
import { toIsoDateTime } from "../utils/date";
import { validateDateRange } from "../utils/validation";
export function DateRangeFilter({ onApply }: { onApply: (from: string, to: string) => void }) {
  const [from, setFrom] = useState(""); const [to, setTo] = useState(""); const [error, setError] = useState<string | null>(null);
  function apply() { const e = validateDateRange(from, to); if (e) { setError(e); return; } setError(null); onApply(toIsoDateTime(from), toIsoDateTime(to)); }
  return <div className="filter-bar"><label>From<input type="datetime-local" value={from} onChange={e => setFrom(e.target.value)} /></label><label>To<input type="datetime-local" value={to} onChange={e => setTo(e.target.value)} /></label><button className="secondary-button" onClick={apply}>Apply</button>{error && <span className="field-error">{error}</span>}</div>;
}
