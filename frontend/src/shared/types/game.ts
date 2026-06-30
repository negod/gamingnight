export type MatchType =
  | 'PLAYER_VS_PLAYER'
  | 'TEAM_VS_TEAM'
  | 'FREE_FOR_ALL'
  | 'SOLO_CHALLENGE'
  | 'COOP_VS_AI';

export type ResultType =
  | 'SCORE'
  | 'TIME'
  | 'PLACEMENT'
  | 'WINNER_ONLY'
  | 'KILLS'
  | 'GOALS'
  | 'ROUNDS_WON'
  | 'CUSTOM_NUMBER';

export type WinnerRule =
  | 'HIGHEST_VALUE_WINS'
  | 'LOWEST_VALUE_WINS'
  | 'FIRST_TO_FINISH_WINS'
  | 'LAST_REMAINING_WINS'
  | 'MOST_ROUNDS_WON'
  | 'MANUAL_WINNER'
  | 'CLOSEST_TO_TARGET';

export type TieBreakerRule =
  | 'ALLOW_DRAW'
  | 'EXTRA_ROUND'
  | 'SUDDEN_DEATH'
  | 'HIGHEST_SECONDARY_VALUE'
  | 'LOWEST_SECONDARY_VALUE'
  | 'MANUAL_DECISION'
  | 'RANDOM';

export type RotationRule =
  | 'NONE'
  | 'CAPTAIN_CHOOSES'
  | 'RANDOM_PLAYER'
  | 'LEAST_PLAYED_PLAYER'
  | 'EVERYONE_MUST_PLAY';

export type BonusCondition =
  | 'PERFECT_SCORE'
  | 'FASTEST_TIME'
  | 'MOST_KILLS'
  | 'BIGGEST_WIN_MARGIN'
  | 'NEW_RECORD'
  | 'CLEAN_SHEET';

export type ScoreRounding = 'NONE' | 'FLOOR' | 'CEIL' | 'ROUND';

export type TimeAction = 'END_MATCH' | 'USE_CURRENT_SCORE' | 'MANUAL_DECISION';

export type ScoringRule =
  | { type: 'WIN_DRAW_LOSS'; pointsForWin: number; pointsForDraw: number; pointsForLoss: number }
  | { type: 'PLACEMENT'; pointsByPlacement: Record<number, number> }
  | { type: 'SCORE_BASED'; multiplier: number; rounding: ScoreRounding }
  | { type: 'WINNER_TAKES_ALL'; pointsToWinner: number }
  | { type: 'MANUAL' };

export type ParticipantRule = {
  minPlayersPerTeam: number;
  maxPlayersPerTeam: number;
  numberOfTeams: number | null;
  allowSubstitutes: boolean;
};

export type ValidationRule = {
  minValue: number | null;
  maxValue: number | null;
  allowDecimals: boolean;
  required: boolean;
};

export type TimeLimitRule = {
  hasTimeLimit: boolean;
  durationMinutes: number | null;
  actionWhenTimeRunsOut: TimeAction;
};

export type BonusRule = {
  name: string;
  condition: BonusCondition;
  bonusPoints: number;
};

export type Game = {
  id: string;
  name: string;
  description: string;
  platform: string | null;
  genre: string | null;
  isActive: boolean;
  matchType: MatchType;
  participantRule: ParticipantRule;
  resultType: ResultType;
  winnerRule: WinnerRule;
  scoringRule: ScoringRule;
  tieBreakerRule: TieBreakerRule;
  validationRule: ValidationRule | null;
  rotationRule: RotationRule | null;
  timeLimitRule: TimeLimitRule | null;
  bonusRules: BonusRule[];
  createdAt: string;
  updatedAt: string;
};

export type GameFormValues = Omit<Game, 'id' | 'createdAt' | 'updatedAt'>;
