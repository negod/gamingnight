import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { PlayerList } from './PlayerList';

describe('PlayerList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <PlayerList players={[]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no players yet/i)).toBeInTheDocument();
  });

  it('renders players and edit links', () => {
    render(
      <MemoryRouter>
        <PlayerList
          players={[player('player-1', 'Alice')]}
          onDelete={vi.fn()}
        />
      </MemoryRouter>,
    );

    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /edit/i })).toHaveAttribute('href', '/players/player-1/edit');
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <PlayerList players={[player('player-1', 'Alice')]} onDelete={onDelete} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(onDelete).toHaveBeenCalledWith('player-1');
  });
});

function player(id: string, name: string) {
  return {
    id,
    name,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
