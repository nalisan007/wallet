import type { WalletActivityResponse } from "./statement";

export interface WalletResponse {
  id: string;
  userId: string | null;
  balancePaise: number;
  status: "ACTIVE" | "INACTIVE" | string;
  createdAt: string;
  recentActivity: WalletActivityResponse[];
}
