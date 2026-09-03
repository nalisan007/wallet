import { BrowserRouter } from "react-router-dom";
import { AppRoutes } from "./routes/AppRoutes";

export default function App() {
  return (
    <BrowserRouter>
      <header className="app-header">
        <div className="shell header-inner">
          <a className="brand" href="/">Wallet</a>
          <span className="brand-subtitle">Transfer MVP</span>
        </div>
      </header>
      <main className="shell main-content">
        <AppRoutes />
      </main>
    </BrowserRouter>
  );
}