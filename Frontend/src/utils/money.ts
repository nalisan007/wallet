export function formatPaise(amountPaise: number): string {
  const sign = amountPaise < 0 ? "-" : "";
  const absolute = Math.abs(amountPaise);
  const rupees = Math.floor(absolute / 100);
  const paise = String(absolute % 100).padStart(2, "0");
  return `${sign}₹${rupees.toLocaleString("en-IN")}.${paise}`;
}