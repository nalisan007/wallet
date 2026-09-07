export function parseRupeesToPaise(value: string): number | null {
  const normalized = value.trim();
  if (!/^\d+(?:\.\d{1,2})?$/.test(normalized)) return null;

  const [rupeesPart, paisePart = ""] = normalized.split(".");
  const paiseText = paisePart.padEnd(2, "0");

  try {
    const paise = BigInt(`${rupeesPart}${paiseText}`);
    if (paise <= 0n || paise > BigInt(Number.MAX_SAFE_INTEGER)) return null;
    return Number(paise);
  } catch {
    return null;
  }
}

export function formatPaise(amountPaise: number): string {
  const negative = amountPaise < 0;
  const absolute = Math.abs(amountPaise);
  const rupees = Math.floor(absolute / 100);
  const paise = String(absolute % 100).padStart(2, "0");
  return `${negative ? "-" : ""}₹${rupees.toLocaleString("en-IN")}.${paise}`;
}
