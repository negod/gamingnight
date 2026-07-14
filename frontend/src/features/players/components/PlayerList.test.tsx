import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { PlayerList } from './PlayerList';

describe('PlayerList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <PlayerList players={[]} onEdit={vi.fn()} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no players yet/i)).toBeInTheDocument();
  });

  it('renders players and edit action', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    render(
      <MemoryRouter>
        <PlayerList
          players={[player('player-1', 'Alice')]}
          onEdit={onEdit}
          onDelete={vi.fn()}
        />
      </MemoryRouter>,
    );

    expect(screen.getByText('Alice')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /edit/i }));
    expect(onEdit).toHaveBeenCalledWith(expect.objectContaining({ id: 'player-1' }));
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <PlayerList players={[player('player-1', 'Alice')]} onEdit={vi.fn()} onDelete={onDelete} />
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
