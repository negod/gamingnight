import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { TeamList } from './TeamList';
import type { Team } from '../../../shared/types/team';

describe('TeamList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <TeamList teams={[]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no teams yet/i)).toBeInTheDocument();
  });

  it('renders teams with player count and edit links', () => {
    render(
      <MemoryRouter>
        <TeamList teams={[team('team-1', 'Team Alpha', ['player-1', 'player-2'])]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByRole('link', { name: /team alpha/i })).toHaveAttribute('href', '/teams/team-1/edit');
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /edit/i })).toHaveAttribute('href', '/teams/team-1/edit');
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <TeamList teams={[team('team-1', 'Team Alpha', ['player-1'])]} onDelete={onDelete} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(onDelete).toHaveBeenCalledWith('team-1');
  });
});

function team(id: string, name: string, playerIds: string[]): Team {
  return {
    id,
    name,
    playerIds,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
