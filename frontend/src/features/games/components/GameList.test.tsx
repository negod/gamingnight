import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { GameList } from './GameList';
import type { Game } from '../../../shared/types/game';

describe('GameList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <GameList games={[]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no games yet/i)).toBeInTheDocument();
  });

  it('renders games with labels and edit links', () => {
    render(
      <MemoryRouter>
        <GameList games={[game('game-1', 'Bowling')]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Bowling')).toBeInTheDocument();
    expect(screen.getByText('Score')).toBeInTheDocument();
    expect(screen.getByText('Sum')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /edit/i })).toHaveAttribute('href', '/games/game-1/edit');
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <GameList games={[game('game-1', 'Bowling')]} onDelete={onDelete} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(onDelete).toHaveBeenCalledWith('game-1');
  });
});

function game(id: string, name: string): Game {
  return {
    id,
    name,
    gameType: 'SCORE_BASED',
    calculationMethod: 'SUM',
    description: '',
  };
}
