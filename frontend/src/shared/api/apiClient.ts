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

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const body = await parseBody<{ message?: string; details?: string[] }>(response);
    throw new ApiError(body.message ?? 'Request failed', response.status, body.details ?? []);
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
