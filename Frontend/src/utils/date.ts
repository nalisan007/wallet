export function toIsoDateTime(localValue: string): string {
  return new Date(localValue).toISOString();
}

export function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}