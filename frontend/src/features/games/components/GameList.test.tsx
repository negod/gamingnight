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

  it('renders games with rule labels and edit links', () => {
    render(
      <MemoryRouter>
        <GameList games={[game('game-1', 'Bowling')]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Bowling')).toBeInTheDocument();
    expect(screen.getByText('Free for All')).toBeInTheDocument();
    expect(screen.getByText('Score')).toBeInTheDocument();
    expect(screen.getByText('Highest wins')).toBeInTheDocument();
    expect(screen.getByText('Active')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /edit/i })).toHaveAttribute('href', '/games/game-1/edit');
  });

  it('shows Inactive badge for inactive games', () => {
    render(
      <MemoryRouter>
        <GameList games={[game('game-1', 'Old Game', false)]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Inactive')).toBeInTheDocument();
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

function game(id: string, name: string, isActive = true): Game {
  return {
    id,
    name,
    description: '',
    platform: null,
    genre: null,
    isActive,
    matchType: 'FREE_FOR_ALL',
    participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 4, numberOfTeams: null, allowSubstitutes: false },
    resultType: 'SCORE',
    winnerRule: 'HIGHEST_VALUE_WINS',
    scoringRule: { type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 },
    tieBreakerRule: 'ALLOW_DRAW',
    validationRule: null,
    rotationRule: 'NONE',
    timeLimitRule: null,
    bonusRules: [],
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  };
}
