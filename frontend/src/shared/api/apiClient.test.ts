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
});
