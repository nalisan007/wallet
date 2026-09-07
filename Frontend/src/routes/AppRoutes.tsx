import { Navigate, Route, Routes } from "react-router-dom";
import { EndpointsPage } from "../pages/EndpointsPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { TransferHistoryPage } from "../pages/TransferHistoryPage";
import { TransferPage } from "../pages/TransferPage";
import { WalletPage } from "../pages/WalletPage";
import { WalletStatementPage } from "../pages/WalletStatementPage";

export function AppRoutes() {
  return <Routes>
    <Route path="/" element={<Navigate to="/api/v1" replace />} />
    <Route path="/api/v1" element={<EndpointsPage />} />
    <Route path="/api/v1/wallets/:walletId" element={<WalletPage />} />
    <Route path="/api/v1/wallets/:walletId/transfer" element={<TransferPage />} />
    <Route path="/api/v1/wallets/:walletId/transfers" element={<TransferHistoryPage />} />
    <Route path="/api/v1/wallets/:walletId/statement" element={<WalletStatementPage />} />
    <Route path="*" element={<NotFoundPage />} />
  </Routes>;
}
