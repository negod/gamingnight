import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiRequest, clearAuthToken, setAuthExpiredHandler, setAuthToken } from './apiClient';

describe('apiRequest', () => {
  afterEach(() => {
    clearAuthToken();
    setAuthExpiredHandler(null);
    vi.restoreAllMocks();
  });

  it('notifies when the API rejects the current session', async () => {
    const onAuthExpired = vi.fn();
    setAuthToken('expired-token');
    setAuthExpiredHandler(onAuthExpired);
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ message: 'Unauthorized' }), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    await expect(apiRequest('/competitions')).rejects.toMatchObject({
      message: 'Unauthorized',
      status: 401,
    });

    expect(onAuthExpired).toHaveBeenCalledOnce();
  });

  it('returns a controlled API error for an empty error response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(null, {
        status: 405,
      }),
    );

    await expect(apiRequest('/auth/login', { method: 'POST' })).rejects.toMatchObject({
      message: 'Request failed with status 405',
      status: 405,
      details: [],
    });
  });

  it('returns a controlled API error for a non-JSON error response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response('<html><body>Method Not Allowed</body></html>', {
        status: 405,
        headers: { 'Content-Type': 'text/html' },
      }),
    );

    await expect(apiRequest('/auth/login', { method: 'POST' })).rejects.toMatchObject({
      message: 'Request failed with status 405',
      status: 405,
      details: [],
    });
  });
});
