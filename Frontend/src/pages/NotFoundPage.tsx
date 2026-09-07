import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <section className="card not-found">
      <span className="eyebrow">404</span>
      <h1>Page not found</h1>
      <p>The requested wallet application page does not exist.</p>
      <Link className="primary-button inline-button" to="/api/v1">View endpoints</Link>
    </section>
  );
}
