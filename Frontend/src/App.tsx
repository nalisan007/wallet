import { Link } from "react-router-dom";
import { AppRoutes } from "./routes/AppRoutes";
export default function App() { return <><header className="app-header"><div className="shell header-inner"><Link className="brand" to="/api/v1">Wallet</Link><span className="brand-subtitle">Transfer MVP</span></div></header><main className="shell main-content"><AppRoutes /></main></>; }
