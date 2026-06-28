import { MemoryRouter } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { UserList } from './UserList';
import type { AppUser, UserRole } from '../../../shared/types/user';

describe('UserList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <UserList users={[]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no users yet/i)).toBeInTheDocument();
  });

  it('renders users with role and player', () => {
    render(
      <MemoryRouter>
        <UserList users={[appUser('user-1', 'admin', 'ADMIN', 'Alice')]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('Admin')).toBeInTheDocument();
    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /edit/i })).toHaveAttribute('href', '/users/user-1/edit');
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <UserList users={[appUser('user-1', 'admin', 'ADMIN', 'Alice')]} onDelete={onDelete} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(onDelete).toHaveBeenCalledWith('user-1');
  });
});

function appUser(id: string, username: string, role: UserRole, playerName: string): AppUser {
  return {
    id,
    username,
    role,
    playerId: 'player-1',
    playerName,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
