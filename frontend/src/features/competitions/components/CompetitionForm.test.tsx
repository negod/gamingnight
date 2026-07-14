import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { CompetitionForm } from './CompetitionForm';
import type { Game } from '../../../shared/types/game';
import type { Team } from '../../../shared/types/team';

describe('CompetitionForm', () => {
  it('submits setup details with selected games in selection order and selected teams', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(
      <CompetitionForm
        games={games}
        teams={teams}
        submitLabel="Create competition"
        onSubmit={onSubmit}
      />,
    );

    await user.type(screen.getByLabelText(/competition name/i), 'Summer Cup');
    await user.type(screen.getByLabelText(/date/i), '2026-07-01');
    await user.click(screen.getByLabelText('Darts'));
    await user.click(screen.getByLabelText('Bowling'));
    await user.click(screen.getByLabelText(/open for player registrations/i));
    await user.click(screen.getByLabelText(/Team Alpha/));
    await user.click(screen.getByRole('button', { name: /create competition/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Summer Cup',
      date: '2026-07-01',
      singleMatch: true,
      registrationOpen: true,
      gameIds: ['game-2', 'game-1'],
      teamIds: ['team-1'],
    });
  });

  it('shows validation error when name is missing', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(
      <CompetitionForm
        games={games}
        teams={teams}
        submitLabel="Create competition"
        onSubmit={onSubmit}
      />,
    );

    await user.click(screen.getByRole('button', { name: /create competition/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Competition name is required');
    expect(onSubmit).not.toHaveBeenCalled();
  });
});

const games: Game[] = [
  game('game-1', 'Bowling'),
  game('game-2', 'Darts'),
];

const teams: Team[] = [
  {
    id: 'team-1',
    name: 'Team Alpha',
    playerIds: ['player-1', 'player-2'],
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  },
];

function game(id: string, name: string): Game {
  return {
    id,
    name,
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
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
