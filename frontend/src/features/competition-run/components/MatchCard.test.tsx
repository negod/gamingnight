import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { MatchCard } from './MatchCard';
import type { Game } from '../../../shared/types/game';
import type { Match } from '../../../shared/types/match';

describe('MatchCard', () => {
  it('renders pending match status and team names', () => {
    render(<MatchCard match={match({ completed: false, results: [] })} game={game('TEAM_VS_TEAM')} />);

    expect(screen.getByText('Pending')).toBeInTheDocument();
    expect(screen.getByText('Alpha')).toBeInTheDocument();
    expect(screen.getByText('Beta')).toBeInTheDocument();
    expect(screen.getByText('VS')).toBeInTheDocument();
  });

  it('renders completed match status and player result names', () => {
    render(<MatchCard match={completedMatch()} game={game('TEAM_VS_TEAM')} />);

    expect(screen.getByText('Done')).toBeInTheDocument();
    expect(screen.getByText('Alpha')).toBeInTheDocument();
    expect(screen.getByText('Beta')).toBeInTheDocument();
    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
    expect(screen.getByText('21')).toBeInTheDocument();
    expect(screen.getByText('13')).toBeInTheDocument();
  });

  it('rounds merged team totals to at most three decimals', () => {
    render(
      <MatchCard
        match={match({
          completed: true,
          results: [
            result('player-1', 'home-team', 'Alice', 'Alpha', 0.1),
            result('player-2', 'home-team', 'Aron', 'Alpha', 0.2),
            result('player-3', 'away-team', 'Bob', 'Beta', 0.1234567),
          ],
        })}
        game={game('TEAM_VS_TEAM')}
      />,
    );

    expect(screen.getByText('0.3 – 0.123')).toBeInTheDocument();
    expect(screen.queryByText(/0\.300000/)).not.toBeInTheDocument();
  });

  it('formats duel values with at most three decimals', () => {
    render(
      <MatchCard
        match={match({
          completed: true,
          results: [
            result('player-1', 'home-team', 'Alice', 'Alpha', 1.23456),
            result('player-2', 'away-team', 'Bob', 'Beta', 2),
          ],
        })}
        game={game('PLAYER_VS_PLAYER')}
      />,
    );

    expect(screen.getByText('1.235')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('renders duel player names and calls edit handler', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    const duelMatch = match({
      completed: true,
      results: [
        result('player-1', 'home-team', 'Alice', 'Alpha', 7),
        result('player-2', 'away-team', 'Bob', 'Beta', 3),
      ],
    });

    render(<MatchCard match={duelMatch} game={game('PLAYER_VS_PLAYER')} onEdit={onEdit} />);

    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
    expect(screen.getByText('Alpha')).toBeInTheDocument();
    expect(screen.getByText('Beta')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /enter results for alice vs bob/i }));

    expect(onEdit).toHaveBeenCalledWith(duelMatch);
  });
});

function completedMatch(): Match {
  return match({
    completed: true,
    results: [
      result('player-1', 'home-team', 'Alice', 'Alpha', 21),
      result('player-2', 'away-team', 'Bob', 'Beta', 13),
    ],
  });
}

function match(overrides: Partial<Match> = {}): Match {
  return {
    id: 'match-1',
    competitionId: 'competition-1',
    gameId: 'game-1',
    homeTeamId: 'home-team',
    homeTeamName: 'Alpha',
    awayTeamId: 'away-team',
    awayTeamName: 'Beta',
    completed: false,
    results: [],
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
    ...overrides,
  };
}

function result(playerId: string, teamId: string, playerName: string, teamName: string, value: number) {
  return { playerId, teamId, playerName, teamName, value };
}

function game(matchType: Game['matchType']): Game {
  return {
    id: 'game-1',
    name: 'Bowling',
    description: '',
    platform: null,
    genre: null,
    isActive: true,
    matchType,
    participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 4, numberOfTeams: null, allowSubstitutes: false },
    resultType: 'SCORE',
    winnerRule: 'HIGHEST_VALUE_WINS',
    scoringRule: { type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 },
    tieBreakerRule: 'ALLOW_DRAW',
    validationRule: null,
    rotationRule: 'NONE',
    timeLimitRule: null,
    bonusRules: [],
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
