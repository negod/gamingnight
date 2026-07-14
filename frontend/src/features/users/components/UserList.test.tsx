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
        <UserList users={[]} onEdit={vi.fn()} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no users yet/i)).toBeInTheDocument();
  });

  it('renders users with email, role and Player callsign', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    render(
      <MemoryRouter>
        <UserList users={[appUser('user-1', 'admin', 'ADMIN', 'Alice', 'admin@example.com')]} onEdit={onEdit} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('admin@example.com')).toBeInTheDocument();
    expect(screen.getByText('Player callsign')).toBeInTheDocument();
    expect(screen.getByText('Admin')).toBeInTheDocument();
    expect(screen.getByText('Alice')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /edit/i }));
    expect(onEdit).toHaveBeenCalledWith(expect.objectContaining({ id: 'user-1' }));
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <UserList users={[appUser('user-1', 'admin', 'ADMIN', 'Alice')]} onEdit={vi.fn()} onDelete={onDelete} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(onDelete).toHaveBeenCalledWith('user-1');
  });
});

function appUser(id: string, username: string, role: UserRole, playerName: string, email: string | null = null): AppUser {
  return {
    id,
    username,
    email,
    role,
    playerId: 'player-1',
    playerName,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
