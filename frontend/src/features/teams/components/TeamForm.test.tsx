import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { TeamForm } from './TeamForm';
import type { Player } from '../../../shared/types/player';

describe('TeamForm', () => {
  it('submits team name and selected players', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<TeamForm players={[player('player-1', 'Alice'), player('player-2', 'Bob')]} submitLabel="Create team" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/team name/i), 'Team Alpha');
    await user.click(screen.getByLabelText('Alice'));
    await user.click(screen.getByLabelText('Bob'));
    await user.click(screen.getByRole('button', { name: /create team/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Team Alpha',
      playerIds: ['player-1', 'player-2'],
    });
  });

  it('shows validation error when name is missing', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<TeamForm players={[player('player-1', 'Alice')]} submitLabel="Create team" onSubmit={onSubmit} />);

    await user.click(screen.getByRole('button', { name: /create team/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Team name is required');
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('renders empty player state', () => {
    render(<TeamForm players={[]} submitLabel="Create team" onSubmit={vi.fn()} />);

    expect(screen.getByText(/no players available/i)).toBeInTheDocument();
  });

  it('filters players while keeping existing team members selected', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(
      <TeamForm
        initialValues={{ name: 'Team Alpha', playerIds: ['player-1'] }}
        players={[player('player-1', 'Alice'), player('player-2', 'Bob'), player('player-3', 'Charlie')]}
        submitLabel="Save team"
        onSubmit={onSubmit}
      />,
    );

    expect(screen.getByLabelText('Alice')).toBeChecked();

    await user.type(screen.getByLabelText(/search players/i), 'bo');
    expect(screen.queryByLabelText('Alice')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Bob')).toBeInTheDocument();

    await user.click(screen.getByLabelText('Bob'));
    await user.click(screen.getByRole('button', { name: /save team/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Team Alpha',
      playerIds: ['player-1', 'player-2'],
    });
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
