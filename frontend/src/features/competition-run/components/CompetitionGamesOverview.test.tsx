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
    referenceUrl: null,
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

  it('renders the admin-authored description as its own paragraph', () => {
    const games = [
      buildGame({ name: 'Wii Bowling', description: 'House rules: no bumpers, best of three games.' }),
    ];

    render(<CompetitionGamesOverview games={games} />);

    expect(screen.getByText('House rules: no bumpers, best of three games.')).toBeInTheDocument();
  });

  it('does not render a description paragraph when it is blank', () => {
    const games = [buildGame({ name: 'Wii Bowling', description: '' })];

    render(<CompetitionGamesOverview games={games} />);

    expect(screen.queryByText('', { selector: 'p' })).not.toBeInTheDocument();
  });

  it('renders a clickable link for a valid http(s) reference URL', () => {
    const games = [
      buildGame({ name: 'Wii Bowling', referenceUrl: 'https://www.youtube.com/watch?v=abc123' }),
    ];

    render(<CompetitionGamesOverview games={games} />);

    const link = screen.getByRole('link', { name: /more about this game/i });
    expect(link).toHaveAttribute('href', 'https://www.youtube.com/watch?v=abc123');
    expect(link).toHaveAttribute('target', '_blank');
    expect(link).toHaveAttribute('rel', 'noopener noreferrer');
  });

  it('does not render a link for an unsafe URL scheme', () => {
    const games = [buildGame({ name: 'Wii Bowling', referenceUrl: 'javascript:alert(1)' })];

    render(<CompetitionGamesOverview games={games} />);

    expect(screen.queryByRole('link')).not.toBeInTheDocument();
  });

  it('does not render a link when no reference URL is set', () => {
    const games = [buildGame({ name: 'Wii Bowling', referenceUrl: null })];

    render(<CompetitionGamesOverview games={games} />);

    expect(screen.queryByRole('link')).not.toBeInTheDocument();
  });
});
