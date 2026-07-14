export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly details: string[] = [],
  ) {
    super(message);
  }
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';
const tokenStorageKey = 'gaming-night-token';
let onAuthExpired: (() => void) | null = null;

export function setAuthExpiredHandler(handler: (() => void) | null): void {
  onAuthExpired = handler;
}

export function getAuthToken(): string | null {
  return localStorage.getItem(tokenStorageKey);
}

export function setAuthToken(token: string): void {
  localStorage.setItem(tokenStorageKey, token);
}

export function clearAuthToken(): void {
  localStorage.removeItem(tokenStorageKey);
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getAuthToken();
  const response = await fetch(`${apiBaseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const body = await parseErrorBody(response);
    if (response.status === 401) {
      onAuthExpired?.();
    }
    throw new ApiError(body.message, response.status, body.details);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return parseBody<T>(response);
}

async function parseBody<T>(response: Response): Promise<T> {
  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

async function parseErrorBody(response: Response): Promise<{ message: string; details: string[] }> {
  const fallback = { message: `Request failed with status ${response.status}`, details: [] };
  const text = await response.text();

  if (!text) {
    return fallback;
  }

  try {
    const body = JSON.parse(text) as { message?: unknown; details?: unknown };
    return {
      message: typeof body.message === 'string' && body.message.trim() ? body.message : fallback.message,
      details: Array.isArray(body.details) ? body.details.filter((detail): detail is string => typeof detail === 'string') : [],
    };
  } catch {
    return fallback;
  }
}
