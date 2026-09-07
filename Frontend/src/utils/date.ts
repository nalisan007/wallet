export function toIsoDateTime(localValue: string): string {
  return new Date(localValue).toISOString().replace(/\.\d{3}Z$/, "Z");
}

export function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "medium",
    timeZone: "Asia/Kolkata"
  }).format(new Date(value));
}
