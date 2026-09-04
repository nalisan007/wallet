import { Navigate, Route, Routes } from "react-router-dom";
import { TransferHistoryPage } from "../pages/TransferHistoryPage";
import { TransferPage } from "../pages/TransferPage";
import { WalletPage } from "../pages/WalletPage";
import { WalletStatementPage } from "../pages/WalletStatementPage";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/api/v1/wallets/:walletId" element={<WalletPage />} />
      <Route path="/api/v1/wallets/:walletId/transfer" element={<TransferPage />} />
      <Route path="/api/v1/wallets/:walletId/transfers" element={<TransferHistoryPage />} />
      <Route path="/api/v1/wallets/:walletId/statement" element={<WalletStatementPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
