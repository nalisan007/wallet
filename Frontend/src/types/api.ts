export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string>;
  timestamp?: string;
}

export interface ApiException extends Error {
  status: number;
  code: string;
  details?: Record<string, string>;
}
