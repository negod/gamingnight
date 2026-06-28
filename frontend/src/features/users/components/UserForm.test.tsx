import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { UserForm } from './UserForm';
import type { Player } from '../../../shared/types/player';

describe('UserForm', () => {
  it('submits trimmed username, role, and player', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<UserForm players={[player('player-1', 'Alice')]} submitLabel="Create user" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/username/i), ' admin ');
    await user.type(screen.getByLabelText(/^password$/i), 'secret');
    await user.selectOptions(screen.getByLabelText(/role/i), 'ADMIN');
    await user.selectOptions(screen.getByLabelText(/player/i), 'player-1');
    await user.click(screen.getByRole('button', { name: /create user/i }));

    expect(onSubmit).toHaveBeenCalledWith({ username: 'admin', password: 'secret', role: 'ADMIN', playerId: 'player-1' });
  });

  it('requires username and player', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<UserForm players={[player('player-1', 'Alice')]} submitLabel="Create user" onSubmit={onSubmit} />);

    await user.click(screen.getByRole('button', { name: /create user/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Username is required');
    expect(onSubmit).not.toHaveBeenCalled();
  });
});

function player(id: string, name: string): Player {
  return {
    id,
    name,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
