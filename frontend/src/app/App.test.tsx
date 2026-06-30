import { MemoryRouter } from 'react-router-dom';
import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';
import { AuthProvider } from '../shared/auth/AuthContext';

describe('App auth redirects', () => {
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('redirects to login when the stored token is rejected', async () => {
    localStorage.setItem('gaming-night-token', 'expired-token');
    localStorage.setItem('gaming-night-user', JSON.stringify({
      id: 'user-1',
      username: 'admin',
      role: 'ADMIN',
      playerId: 'player-1',
      playerName: 'Alice',
      createdAt: '2026-01-01T10:00:00Z',
      updatedAt: '2026-01-01T10:00:00Z',
    }));
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ message: 'Unauthorized' }), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    render(
      <MemoryRouter initialEntries={['/competitions']}>
        <AuthProvider>
          <App />
        </AuthProvider>
      </MemoryRouter>,
    );

    expect(await screen.findByRole('heading', { name: /sign in/i })).toBeInTheDocument();
    await waitFor(() => expect(localStorage.getItem('gaming-night-token')).toBeNull());
    expect(localStorage.getItem('gaming-night-user')).toBeNull();
  });
});
