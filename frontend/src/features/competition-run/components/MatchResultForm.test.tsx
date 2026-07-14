import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { MatchResultForm } from './MatchResultForm';
import type { Game } from '../../../shared/types/game';
import type { Match } from '../../../shared/types/match';
import type { PlayerRow } from '../hooks/useMatchDetails';

describe('MatchResultForm', () => {
  it('renders all result inputs for grouped matches', () => {
    render(<MatchResultForm match={match()} game={game('TEAM_VS_TEAM')} rows={rows()} onSave={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByLabelText('Score for Alice')).toBeInTheDocument();
    expect(screen.getByLabelText('Score for Aron')).toBeInTheDocument();
    expect(screen.getByLabelText('Score for Bob')).toBeInTheDocument();
    expect(screen.getByLabelText('Score for Bea')).toBeInTheDocument();
  });

  it('submits updated result payload', async () => {
    const user = userEvent.setup();
    const onSave = vi.fn().mockResolvedValue(undefined);

    render(<MatchResultForm match={match()} game={game('TEAM_VS_TEAM')} rows={rows()} onSave={onSave} onCancel={vi.fn()} />);

    await replaceValue(user, 'Score for Alice', '12.5');
    await replaceValue(user, 'Score for Aron', '8');
    await replaceValue(user, 'Score for Bob', '10');
    await replaceValue(user, 'Score for Bea', '6');
    await user.click(screen.getByRole('button', { name: /save results/i }));

    await waitFor(() => expect(onSave).toHaveBeenCalledTimes(1));
    expect(onSave).toHaveBeenCalledWith([
      { playerId: 'player-1', teamId: 'home-team', playerName: 'Alice', teamName: 'Alpha', value: 12.5 },
      { playerId: 'player-2', teamId: 'home-team', playerName: 'Aron', teamName: 'Alpha', value: 8 },
      { playerId: 'player-3', teamId: 'away-team', playerName: 'Bob', teamName: 'Beta', value: 10 },
      { playerId: 'player-4', teamId: 'away-team', playerName: 'Bea', teamName: 'Beta', value: 6 },
    ]);
  });

  it('renders duel inputs and submits duel payload', async () => {
    const user = userEvent.setup();
    const onSave = vi.fn().mockResolvedValue(undefined);

    render(
      <MatchResultForm
        match={match()}
        game={game('PLAYER_VS_PLAYER')}
        rows={[rows()[0], rows()[2]]}
        onSave={onSave}
        onCancel={vi.fn()}
      />,
    );

    expect(screen.getByLabelText('Score for Alice')).toBeInTheDocument();
    expect(screen.getByLabelText('Score for Bob')).toBeInTheDocument();

    await replaceValue(user, 'Score for Alice', '4');
    await replaceValue(user, 'Score for Bob', '9');
    await user.click(screen.getByRole('button', { name: /save results/i }));

    await waitFor(() => expect(onSave).toHaveBeenCalledTimes(1));
    expect(onSave).toHaveBeenCalledWith([
      { playerId: 'player-1', teamId: 'home-team', playerName: 'Alice', teamName: 'Alpha', value: 4 },
      { playerId: 'player-3', teamId: 'away-team', playerName: 'Bob', teamName: 'Beta', value: 9 },
    ]);
  });

  it('calls onCancel when cancelling', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();

    render(<MatchResultForm match={match()} game={game('TEAM_VS_TEAM')} rows={rows()} onSave={vi.fn()} onCancel={onCancel} />);

    await user.click(screen.getAllByRole('button', { name: /^cancel$/i })[0]);

    expect(onCancel).toHaveBeenCalledTimes(1);
  });
});

async function replaceValue(user: ReturnType<typeof userEvent.setup>, label: string, value: string) {
  const input = screen.getByLabelText(label);
  await user.clear(input);
  await user.type(input, value);
}

function match(): Match {
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
  };
}

function rows(): PlayerRow[] {
  return [
    { playerId: 'player-1', teamId: 'home-team', playerName: 'Alice', teamName: 'Alpha', value: '' },
    { playerId: 'player-2', teamId: 'home-team', playerName: 'Aron', teamName: 'Alpha', value: '' },
    { playerId: 'player-3', teamId: 'away-team', playerName: 'Bob', teamName: 'Beta', value: '' },
    { playerId: 'player-4', teamId: 'away-team', playerName: 'Bea', teamName: 'Beta', value: '' },
  ];
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
