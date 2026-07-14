import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { CompetitionGamesOverview } from './CompetitionGamesOverview';
import type { Game } from '../../../shared/types/game';

function buildGame(overrides: Partial<Game>): Game {
  return {
    id: 'game-1',
    name: 'Test Game',
    description: '',
    platform: null,
    genre: null,
    isActive: true,
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
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  };
}

describe('CompetitionGamesOverview', () => {
  it('renders nothing when there are no games', () => {
    const { container } = render(<CompetitionGamesOverview games={[]} />);
    expect(container).toBeEmptyDOMElement();
  });

  it('renders each game name with its rule description', () => {
    const games = [
      buildGame({ id: 'g1', name: 'Wii Bowling', matchType: 'PLAYER_VS_PLAYER' }),
      buildGame({ id: 'g2', name: 'Mario Kart', matchType: 'FREE_FOR_ALL', resultType: 'PLACEMENT' }),
    ];

    render(<CompetitionGamesOverview games={games} />);

    expect(screen.getByRole('heading', { name: /games in this competition/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Wii Bowling' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Mario Kart' })).toBeInTheDocument();
    expect(screen.getByText(/Played 1v1 — two players go head-to-head\./)).toBeInTheDocument();
    expect(screen.getByText(/finishing position/)).toBeInTheDocument();
  });
});
