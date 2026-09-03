import { useState } from "react";
import { toIsoDateTime } from "../utils/date";
import { validateDateRange } from "../utils/validation";

export function DateRangeFilter({
  onApply
}: {
  onApply: (from: string, to: string) => void;
}) {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [error, setError] = useState<string | null>(null);

  function apply() {
    const validationError = validateDateRange(from, to);
    if (validationError) {
      setError(validationError);
      return;
    }

    setError(null);
    onApply(toIsoDateTime(from), toIsoDateTime(to));
  }

  return (
    <div className="filter-bar">
      <label>
        From
        <input
          type="datetime-local"
          value={from}
          onChange={(event) => setFrom(event.target.value)}
        />
      </label>

      <label>
        To
        <input
          type="datetime-local"
          value={to}
          onChange={(event) => setTo(event.target.value)}
        />
      </label>

      <button className="secondary-button" onClick={apply}>
        Apply
      </button>

      {error && <span className="field-error">{error}</span>}
    </div>
  );
}