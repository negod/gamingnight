import type {
  BonusCondition,
  BonusRule,
  Game,
  ResultType,
  RotationRule,
  ScoringRule,
  TieBreakerRule,
  TimeAction,
  TimeLimitRule,
  WinnerRule,
} from '../../../shared/types/game';

const RESULT_TYPE_LABEL: Record<ResultType, string> = {
  SCORE: 'a numeric score',
  TIME: 'a completion time',
  PLACEMENT: 'a finishing position',
  WINNER_ONLY: 'simply who won',
  KILLS: 'a kill count',
  GOALS: 'a goal count',
  ROUNDS_WON: 'the number of rounds won',
  CUSTOM_NUMBER: 'a custom numeric result',
};

const WINNER_RULE_CLAUSE: Record<WinnerRule, string> = {
  HIGHEST_VALUE_WINS: 'the highest value wins',
  LOWEST_VALUE_WINS: 'the lowest value wins',
  FIRST_TO_FINISH_WINS: 'whoever finishes first wins',
  LAST_REMAINING_WINS: 'the last one standing wins',
  MOST_ROUNDS_WON: 'whoever wins the most rounds takes the match',
  CLOSEST_TO_TARGET: 'the result closest to the target value wins',
  MANUAL_WINNER: 'an admin decides the winner',
};

const TIE_BREAKER_CLAUSE: Record<TieBreakerRule, string> = {
  ALLOW_DRAW: 'a tied result stands as a draw',
  EXTRA_ROUND: 'an extra round is played to decide it',
  SUDDEN_DEATH: 'it goes to sudden death',
  HIGHEST_SECONDARY_VALUE: 'a secondary value (higher is better) decides it',
  LOWEST_SECONDARY_VALUE: 'a secondary value (lower is better) decides it',
  MANUAL_DECISION: 'an admin decides the outcome',
  RANDOM: 'it is settled at random',
};

const TIME_ACTION_CLAUSE: Record<TimeAction, string> = {
  END_MATCH: 'the match ends immediately and whoever is leading wins',
  USE_CURRENT_SCORE: 'the current score is locked in as the final result',
  MANUAL_DECISION: 'an admin decides what happens next',
};

const BONUS_CONDITION_LABEL: Record<BonusCondition, string> = {
  PERFECT_SCORE: 'a perfect score',
  FASTEST_TIME: 'the fastest time in the session',
  MOST_KILLS: 'the most kills',
  BIGGEST_WIN_MARGIN: 'the biggest win margin',
  NEW_RECORD: 'a new personal record',
  CLEAN_SHEET: 'a clean sheet',
};

function ordinal(place: number): string {
  if (place === 1) return '1st';
  if (place === 2) return '2nd';
  if (place === 3) return '3rd';
  return `${place}th`;
}

function participantsSentence(game: Game): string {
  const { minPlayersPerTeam: min, maxPlayersPerTeam: max, numberOfTeams, allowSubstitutes } = game.participantRule;
  const squadSize = min === max ? `${min}` : `${min}–${max}`;

  let sentence: string;
  switch (game.matchType) {
    case 'PLAYER_VS_PLAYER':
      sentence = 'Played 1v1 — two players go head-to-head.';
      break;
    case 'TEAM_VS_TEAM':
      sentence = `Played between teams of ${squadSize} player${max === 1 ? '' : 's'}`;
      sentence += numberOfTeams ? `, ${numberOfTeams} teams per match.` : '.';
      break;
    case 'FREE_FOR_ALL':
      sentence = 'A free-for-all — three or more participants play at once and are ranked against each other.';
      break;
    case 'SOLO_CHALLENGE':
      sentence = "A solo challenge — each player competes alone and is ranked on their own result.";
      break;
    case 'COOP_VS_AI':
      sentence = `Players team up in groups of ${squadSize} to beat the game or an AI opponent together.`;
      break;
  }
  if (allowSubstitutes) sentence += ' Substitutes are allowed mid-match.';
  return sentence;
}

function resultSentence(game: Game): string {
  if (game.resultType === 'WINNER_ONLY') {
    return `No numeric result is recorded — ${WINNER_RULE_CLAUSE[game.winnerRule]}.`;
  }
  return `Results are entered as ${RESULT_TYPE_LABEL[game.resultType]}, and ${WINNER_RULE_CLAUSE[game.winnerRule]}.`;
}

function tieBreakerSentence(game: Game): string {
  return `If the result is tied, ${TIE_BREAKER_CLAUSE[game.tieBreakerRule]}.`;
}

function scoringSentence(rule: ScoringRule): string {
  switch (rule.type) {
    case 'WIN_DRAW_LOSS':
      return `Towards the competition standings, a win earns ${rule.pointsForWin} pts, a draw ${rule.pointsForDraw} pts, and a loss ${rule.pointsForLoss} pts.`;
    case 'PLACEMENT': {
      const parts = Object.entries(rule.pointsByPlacement)
        .sort(([a], [b]) => +a - +b)
        .map(([place, pts]) => `${ordinal(+place)} place ${pts} pts`);
      return `Leaderboard points are awarded by finishing position: ${parts.join(', ')}.`;
    }
    case 'SCORE_BASED':
      return rule.multiplier === 1
        ? 'The raw result is used directly as leaderboard points.'
        : `The raw result is converted into leaderboard points using a ×${rule.multiplier} multiplier.`;
    case 'WINNER_TAKES_ALL':
      return `Only the winner earns leaderboard points (${rule.pointsToWinner} pts) — everyone else gets zero.`;
    case 'MANUAL':
      return 'An admin manually assigns leaderboard points after each match.';
  }
}

function timeLimitSentence(rule: TimeLimitRule | null): string | null {
  if (!rule?.hasTimeLimit) return null;
  return `Each match has a ${rule.durationMinutes}-minute time limit; if time runs out, ${TIME_ACTION_CLAUSE[rule.actionWhenTimeRunsOut]}.`;
}

function bonusSentence(bonusRules: BonusRule[]): string | null {
  if (bonusRules.length === 0) return null;
  const parts = bonusRules.map((bonus) => `${bonus.bonusPoints} bonus pts for ${BONUS_CONDITION_LABEL[bonus.condition]}`);
  return `Extra points are up for grabs: ${parts.join(', ')}.`;
}

function rotationSentence(rule: RotationRule | null): string | null {
  if (rule === 'EVERYONE_MUST_PLAY') return 'Everyone plays at least one match before anyone plays a second.';
  if (rule === 'LEAST_PLAYED_PLAYER') return 'Whoever has played the fewest matches so far gets priority.';
  return null;
}

export function describeGameRules(game: Game): string {
  return [
    participantsSentence(game),
    resultSentence(game),
    tieBreakerSentence(game),
    scoringSentence(game.scoringRule),
    timeLimitSentence(game.timeLimitRule),
    bonusSentence(game.bonusRules),
    rotationSentence(game.rotationRule),
  ]
    .filter((sentence): sentence is string => Boolean(sentence))
    .join(' ');
}
