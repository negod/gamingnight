import type { GameFormValues } from '../../../shared/types/game';

export type GameRulePresetId =
  | 'racing'
  | 'sports'
  | 'action'
  | 'fighting'
  | 'strategy'
  | 'party'
  | 'coop';

export type GameRulePreset = {
  id: GameRulePresetId;
  name: string;
  summary: string;
  values: Pick<
    GameFormValues,
    | 'genre'
    | 'matchType'
    | 'participantRule'
    | 'resultType'
    | 'winnerRule'
    | 'scoringRule'
    | 'tieBreakerRule'
    | 'validationRule'
    | 'rotationRule'
    | 'timeLimitRule'
    | 'bonusRules'
  >;
};

export const GAME_RULE_PRESETS: GameRulePreset[] = [
  {
    id: 'racing',
    name: 'Racing',
    summary: 'Placement or time-trial games where finishing position decides the leaderboard.',
    values: {
      genre: 'Racing',
      matchType: 'FREE_FOR_ALL',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 1, numberOfTeams: null, allowSubstitutes: false },
      resultType: 'PLACEMENT',
      winnerRule: 'LOWEST_VALUE_WINS',
      scoringRule: { type: 'PLACEMENT', pointsByPlacement: { 1: 10, 2: 7, 3: 5, 4: 3, 5: 1 } },
      tieBreakerRule: 'LOWEST_SECONDARY_VALUE',
      validationRule: { minValue: 1, maxValue: null, allowDecimals: false, required: true },
      rotationRule: 'EVERYONE_MUST_PLAY',
      timeLimitRule: null,
      bonusRules: [{ name: 'Fastest lap', condition: 'FASTEST_TIME', bonusPoints: 1 }],
    },
  },
  {
    id: 'sports',
    name: 'Sports',
    summary: 'Goal, score, or match-based sports games with standard win/draw/loss points.',
    values: {
      genre: 'Sports',
      matchType: 'TEAM_VS_TEAM',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 4, numberOfTeams: 2, allowSubstitutes: true },
      resultType: 'GOALS',
      winnerRule: 'HIGHEST_VALUE_WINS',
      scoringRule: { type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 },
      tieBreakerRule: 'EXTRA_ROUND',
      validationRule: { minValue: 0, maxValue: null, allowDecimals: false, required: true },
      rotationRule: 'CAPTAIN_CHOOSES',
      timeLimitRule: { hasTimeLimit: true, durationMinutes: 10, actionWhenTimeRunsOut: 'USE_CURRENT_SCORE' },
      bonusRules: [{ name: 'Clean sheet', condition: 'CLEAN_SHEET', bonusPoints: 1 }],
    },
  },
  {
    id: 'action',
    name: 'Action / shooter',
    summary: 'Kill-count or elimination games where higher numbers or last remaining wins.',
    values: {
      genre: 'Action',
      matchType: 'FREE_FOR_ALL',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 2, numberOfTeams: null, allowSubstitutes: false },
      resultType: 'KILLS',
      winnerRule: 'HIGHEST_VALUE_WINS',
      scoringRule: { type: 'SCORE_BASED', multiplier: 1, rounding: 'NONE' },
      tieBreakerRule: 'SUDDEN_DEATH',
      validationRule: { minValue: 0, maxValue: null, allowDecimals: false, required: true },
      rotationRule: 'LEAST_PLAYED_PLAYER',
      timeLimitRule: { hasTimeLimit: true, durationMinutes: 8, actionWhenTimeRunsOut: 'USE_CURRENT_SCORE' },
      bonusRules: [{ name: 'Most kills', condition: 'MOST_KILLS', bonusPoints: 2 }],
    },
  },
  {
    id: 'fighting',
    name: 'Fighting',
    summary: 'Head-to-head rounds where the winner is decided by rounds won.',
    values: {
      genre: 'Fighting',
      matchType: 'PLAYER_VS_PLAYER',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 1, numberOfTeams: 2, allowSubstitutes: false },
      resultType: 'ROUNDS_WON',
      winnerRule: 'MOST_ROUNDS_WON',
      scoringRule: { type: 'WINNER_TAKES_ALL', pointsToWinner: 3 },
      tieBreakerRule: 'EXTRA_ROUND',
      validationRule: { minValue: 0, maxValue: 5, allowDecimals: false, required: true },
      rotationRule: 'EVERYONE_MUST_PLAY',
      timeLimitRule: null,
      bonusRules: [],
    },
  },
  {
    id: 'strategy',
    name: 'Strategy',
    summary: 'Longer tactical matches where an admin can decide or enter final points.',
    values: {
      genre: 'Strategy',
      matchType: 'TEAM_VS_TEAM',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 3, numberOfTeams: 2, allowSubstitutes: false },
      resultType: 'WINNER_ONLY',
      winnerRule: 'MANUAL_WINNER',
      scoringRule: { type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 },
      tieBreakerRule: 'MANUAL_DECISION',
      validationRule: null,
      rotationRule: 'CAPTAIN_CHOOSES',
      timeLimitRule: { hasTimeLimit: true, durationMinutes: 30, actionWhenTimeRunsOut: 'MANUAL_DECISION' },
      bonusRules: [],
    },
  },
  {
    id: 'party',
    name: 'Party / arcade',
    summary: 'Quick score-based games where every participant can rotate through.',
    values: {
      genre: 'Party',
      matchType: 'FREE_FOR_ALL',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 4, numberOfTeams: null, allowSubstitutes: false },
      resultType: 'SCORE',
      winnerRule: 'HIGHEST_VALUE_WINS',
      scoringRule: { type: 'SCORE_BASED', multiplier: 1, rounding: 'NONE' },
      tieBreakerRule: 'HIGHEST_SECONDARY_VALUE',
      validationRule: { minValue: 0, maxValue: null, allowDecimals: false, required: true },
      rotationRule: 'EVERYONE_MUST_PLAY',
      timeLimitRule: null,
      bonusRules: [{ name: 'Perfect score', condition: 'PERFECT_SCORE', bonusPoints: 2 }],
    },
  },
  {
    id: 'coop',
    name: 'Co-op challenge',
    summary: 'Shared attempts against the game or AI where completion can be judged manually.',
    values: {
      genre: 'Co-op',
      matchType: 'COOP_VS_AI',
      participantRule: { minPlayersPerTeam: 2, maxPlayersPerTeam: 4, numberOfTeams: 1, allowSubstitutes: true },
      resultType: 'WINNER_ONLY',
      winnerRule: 'MANUAL_WINNER',
      scoringRule: { type: 'MANUAL' },
      tieBreakerRule: 'MANUAL_DECISION',
      validationRule: null,
      rotationRule: 'LEAST_PLAYED_PLAYER',
      timeLimitRule: { hasTimeLimit: true, durationMinutes: 20, actionWhenTimeRunsOut: 'MANUAL_DECISION' },
      bonusRules: [{ name: 'New record', condition: 'NEW_RECORD', bonusPoints: 2 }],
    },
  },
];
