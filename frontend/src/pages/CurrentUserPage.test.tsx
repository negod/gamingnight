import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CurrentUserPage } from './CurrentUserPage';
import { AuthProvider } from '../shared/auth/AuthContext';
import type { AppUser } from '../shared/types/user';
import { getCurrentUser, updateCurrentUser } from '../features/users/api/usersApi';

vi.mock('../features/users/api/usersApi', () => ({
  getCurrentUser: vi.fn(),
  updateCurrentUser: vi.fn(),
}));

const mockedGetCurrentUser = vi.mocked(getCurrentUser);
const mockedUpdateCurrentUser = vi.mocked(updateCurrentUser);

describe('CurrentUserPage', () => {
  afterEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('shows editable email and Player callsign without showing role', async () => {
    mockedGetCurrentUser.mockResolvedValue(appUser({ role: 'USER', playerName: 'Alice' }));

    renderPage();

    expect(await screen.findByText('alice')).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toHaveValue('alice@example.com');
    expect(screen.getByLabelText(/player callsign/i)).toHaveValue('Alice');
    expect(screen.queryByText('Role')).not.toBeInTheDocument();
    expect(screen.queryByText(/^User$/)).not.toBeInTheDocument();
  });

  it('saves email and Player callsign and updates stored user', async () => {
    const user = userEvent.setup();
    mockedGetCurrentUser.mockResolvedValue(appUser({ role: 'USER', playerName: 'Alice' }));
    mockedUpdateCurrentUser.mockResolvedValue(appUser({
      role: 'USER',
      email: 'new@example.com',
      playerName: 'Ace',
    }));

    renderPage();

    await user.clear(await screen.findByLabelText(/email/i));
    await user.type(screen.getByLabelText(/email/i), ' new@example.com ');
    await user.clear(screen.getByLabelText(/player callsign/i));
    await user.type(screen.getByLabelText(/player callsign/i), ' Ace ');
    await user.click(screen.getByRole('button', { name: /save changes/i }));

    expect(mockedUpdateCurrentUser).toHaveBeenCalledWith({
      email: 'new@example.com',
      playerCallsign: 'Ace',
    });
    await waitFor(() => {
      expect(JSON.parse(localStorage.getItem('gaming-night-user') ?? '{}')).toMatchObject({
        email: 'new@example.com',
        playerName: 'Ace',
      });
    });
  });

  it('requires Player callsign', async () => {
    const user = userEvent.setup();
    mockedGetCurrentUser.mockResolvedValue(appUser({ role: 'USER', playerName: 'Alice' }));

    renderPage();

    await user.clear(await screen.findByLabelText(/player callsign/i));
    await user.click(screen.getByRole('button', { name: /save changes/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Player callsign is required');
    expect(mockedUpdateCurrentUser).not.toHaveBeenCalled();
  });
});

function renderPage() {
  localStorage.setItem('gaming-night-user', JSON.stringify(appUser({ role: 'USER', playerName: 'Alice' })));
  render(
    <AuthProvider>
      <CurrentUserPage />
    </AuthProvider>,
  );
}

function appUser(overrides: Partial<AppUser>): AppUser {
  return {
    id: 'user-1',
    username: 'alice',
    email: 'alice@example.com',
    role: 'USER',
    playerId: 'player-1',
    playerName: 'Alice',
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
    ...overrides,
  };
}
