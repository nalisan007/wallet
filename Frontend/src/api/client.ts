import type { ApiError, ApiException } from "../types/api";

const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1"
).replace(/\/$/, "");

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers
    }
  });

  if (response.ok) {
    return response.status === 204
      ? (undefined as T)
      : (await response.json() as T);
  }

  let errorBody: ApiError | undefined;
  try {
    errorBody = (await response.json()) as ApiError;
  } catch {
    errorBody = undefined;
  }

  const error = new Error(
    errorBody?.message || `Request failed with status ${response.status}`
  ) as ApiException;
  error.status = response.status;
  error.code = errorBody?.code || "HTTP_ERROR";
  error.details = errorBody?.details;
  throw error;
}

export const apiClient = {
  get<T>(path: string) {
    return request<T>(path);
  },
  post<T>(path: string, body: unknown, headers: Record<string, string> = {}) {
    return request<T>(path, {
      method: "POST",
      body: JSON.stringify(body),
      headers
    });
  }
};
