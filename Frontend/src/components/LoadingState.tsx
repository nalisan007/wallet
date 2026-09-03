export function LoadingState({ label = "Loading..." }: { label?: string }) {
  return <div className="state-card" role="status">{label}</div>;
}