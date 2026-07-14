import { describe, expect, it } from 'vitest';
import { describeGameRules } from './gameRuleDescription';
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

describe('describeGameRules', () => {
  it('describes a 1v1 duel with a time limit and a bonus rule', () => {
    const game = buildGame({
      matchType: 'PLAYER_VS_PLAYER',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 1, numberOfTeams: 2, allowSubstitutes: false },
      resultType: 'TIME',
      winnerRule: 'LOWEST_VALUE_WINS',
      scoringRule: { type: 'SCORE_BASED', multiplier: 1, rounding: 'NONE' },
      tieBreakerRule: 'SUDDEN_DEATH',
      timeLimitRule: { hasTimeLimit: true, durationMinutes: 8, actionWhenTimeRunsOut: 'USE_CURRENT_SCORE' },
      bonusRules: [{ name: 'Fastest lap', condition: 'FASTEST_TIME', bonusPoints: 1 }],
    });

    const description = describeGameRules(game);

    expect(description).toContain('Played 1v1 — two players go head-to-head.');
    expect(description).toContain('Results are entered as a completion time, and the lowest value wins.');
    expect(description).toContain('If the result is tied, it goes to sudden death.');
    expect(description).toContain('The raw result is used directly as leaderboard points.');
    expect(description).toContain('Each match has a 8-minute time limit; if time runs out, the current score is locked in as the final result.');
    expect(description).toContain('Extra points are up for grabs: 1 bonus pts for the fastest time in the session.');
  });

  it('describes a team match with win/draw/loss points and mandatory rotation', () => {
    const game = buildGame({
      matchType: 'TEAM_VS_TEAM',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 4, numberOfTeams: 2, allowSubstitutes: true },
      resultType: 'GOALS',
      winnerRule: 'HIGHEST_VALUE_WINS',
      scoringRule: { type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 },
      tieBreakerRule: 'ALLOW_DRAW',
      rotationRule: 'EVERYONE_MUST_PLAY',
    });

    const description = describeGameRules(game);

    expect(description).toContain('Played between teams of 1–4 players, 2 teams per match.');
    expect(description).toContain('Substitutes are allowed mid-match.');
    expect(description).toContain('Results are entered as a goal count, and the highest value wins.');
    expect(description).toContain('If the result is tied, a tied result stands as a draw.');
    expect(description).toContain('a win earns 3 pts, a draw 1 pts, and a loss 0 pts.');
    expect(description).toContain('Everyone plays at least one match before anyone plays a second.');
  });

  it('describes placement scoring ordered by finishing position', () => {
    const game = buildGame({
      matchType: 'FREE_FOR_ALL',
      resultType: 'PLACEMENT',
      winnerRule: 'LOWEST_VALUE_WINS',
      scoringRule: { type: 'PLACEMENT', pointsByPlacement: { 2: 7, 1: 10, 3: 5 } },
    });

    const description = describeGameRules(game);

    expect(description).toContain('Leaderboard points are awarded by finishing position: 1st place 10 pts, 2nd place 7 pts, 3rd place 5 pts.');
  });

  it('describes winner-only results and manual scoring without a numeric result', () => {
    const game = buildGame({
      matchType: 'COOP_VS_AI',
      participantRule: { minPlayersPerTeam: 2, maxPlayersPerTeam: 4, numberOfTeams: 1, allowSubstitutes: false },
      resultType: 'WINNER_ONLY',
      winnerRule: 'MANUAL_WINNER',
      scoringRule: { type: 'MANUAL' },
    });

    const description = describeGameRules(game);

    expect(description).toContain('Players team up in groups of 2–4 to beat the game or an AI opponent together.');
    expect(description).toContain('No numeric result is recorded — an admin decides the winner.');
    expect(description).toContain('An admin manually assigns leaderboard points after each match.');
  });

  it('omits time limit and bonus sentences when neither is configured', () => {
    const game = buildGame({});

    const description = describeGameRules(game);

    expect(description).not.toContain('time limit');
    expect(description).not.toContain('bonus');
  });
});
