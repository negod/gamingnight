import { Save, Wand2 } from 'lucide-react';
import { FormEvent, ReactNode, useState } from 'react';
import type {
  BonusCondition,
  BonusRule,
  GameFormValues,
  MatchType,
  ResultType,
  RotationRule,
  ScoringRule,
  TieBreakerRule,
  TimeAction,
  WinnerRule,
} from '../../../shared/types/game';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import { GAME_RULE_PRESETS, type GameRulePresetId } from './gameRulePresets';

type GameFormProps = {
  initialValues?: GameFormValues;
  submitLabel: string;
  onSubmit: (values: GameFormValues) => Promise<void>;
};

const defaultValues: GameFormValues = {
  name: '',
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
};

function defaultScoringRule(type: ScoringRule['type']): ScoringRule {
  switch (type) {
    case 'WIN_DRAW_LOSS': return { type, pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 };
    case 'PLACEMENT': return { type, pointsByPlacement: { 1: 10, 2: 7, 3: 5, 4: 3 } };
    case 'SCORE_BASED': return { type, multiplier: 1.0, rounding: 'NONE' };
    case 'WINNER_TAKES_ALL': return { type, pointsToWinner: 3 };
    case 'MANUAL': return { type };
  }
}

// ─── Description maps ──────────────────────────────────────────────────────────

const MATCH_TYPE_DESC: Record<MatchType, string> = {
  PLAYER_VS_PLAYER:
    'Two individual players go head-to-head. Classic duel format — one winner, one loser. E.g. Street Fighter, FIFA 1v1, table tennis.',
  TEAM_VS_TEAM:
    'Two teams compete against each other. Each team submits a combined or representative result. E.g. team bowling, Warcraft II 2v2.',
  FREE_FOR_ALL:
    'Three or more participants compete at once. Everyone is ranked against each other. E.g. Mario Kart with 4 players, Wii Bowling nights.',
  SOLO_CHALLENGE:
    'Each player competes alone and their personal result is recorded on a leaderboard. No direct head-to-head — you race against the clock or yourself. E.g. Assetto Corsa time trials.',
  COOP_VS_AI:
    'Players team up to beat a game or AI opponent together. The outcome is a shared win or loss for the whole group.',
};

const RESULT_TYPE_DESC: Record<ResultType, string> = {
  SCORE:
    'A numeric score entered after the match. E.g. Wii Bowling pins (0–300), Guitar Hero percentage, or any point-based result.',
  TIME:
    'A duration — lower is usually better. E.g. Mario Kart lap time, Assetto Corsa race time in seconds. Make sure to also set Winner rule to "Lowest value wins".',
  PLACEMENT:
    'Players or teams are ranked by finishing position (1st, 2nd, 3rd…) rather than a raw number. The system awards points per position using the scoring table you define.',
  WINNER_ONLY:
    'Only who won is recorded — no numeric result is needed. Good for games where the score is irrelevant or not tracked, e.g. chess or Pokémon battles.',
  KILLS:
    'A kill or frag count. E.g. Call of Duty deathmatch, Halo, Warcraft II units destroyed.',
  GOALS:
    'A goal count. E.g. FIFA, Rocket League, table football. Choose "Highest value wins" as the winner rule.',
  ROUNDS_WON:
    'The number of rounds, sets, or games won within the match. E.g. tennis sets, best-of-5 rounds, Street Fighter rounds.',
  CUSTOM_NUMBER:
    'Any numeric value that does not fit the other categories. You define what it means — the system just records and ranks it.',
};

const WINNER_RULE_DESC: Record<WinnerRule, string> = {
  HIGHEST_VALUE_WINS:
    'The team or player with the biggest number wins. Use for scores, kills, goals — anything where more = better.',
  LOWEST_VALUE_WINS:
    'The team or player with the smallest number wins. Use for times and golf strokes — anything where less = better.',
  FIRST_TO_FINISH_WINS:
    'The winner is whoever completes an objective first, regardless of score. E.g. first to reach a kill target, first to finish a race.',
  LAST_REMAINING_WINS:
    'The last team or player still active wins. Used for elimination and battle royale formats.',
  MOST_ROUNDS_WON:
    'Whoever wins the most individual rounds within the match wins overall. E.g. best-of-5 format.',
  CLOSEST_TO_TARGET:
    'The result closest to a specific target value wins. E.g. closest to 301 in darts. Enable "Result validation" below and set the target as the max value.',
  MANUAL_WINNER:
    'An admin or referee manually designates the winner after the match. No automatic calculation is performed.',
};

const TIE_BREAKER_DESC: Record<TieBreakerRule, string> = {
  ALLOW_DRAW:
    'A tied result stands — both teams share draw points. No extra play required. Good for league formats where draws are a valid outcome.',
  EXTRA_ROUND:
    'Play an additional round under the same rules to break the tie. Repeats until a winner emerges.',
  SUDDEN_DEATH:
    'Play continues in a sudden-death format — the first team or player to score or achieve anything wins instantly.',
  HIGHEST_SECONDARY_VALUE:
    'Break the tie using a second metric where a higher value is better, e.g. goal difference, score margin, or kill count.',
  LOWEST_SECONDARY_VALUE:
    'Break the tie using a second metric where a lower value is better, e.g. fastest time among the tied players.',
  MANUAL_DECISION:
    'An admin or referee reviews the match and manually decides the winner. Use when no automatic rule fits.',
  RANDOM:
    'Coin flip or random draw — only use this as a last resort when all other tie-breakers have failed or are not appropriate.',
};

const SCORING_TYPE_DESC: Record<ScoringRule['type'], string> = {
  WIN_DRAW_LOSS:
    'Teams earn a fixed number of points for winning, drawing, or losing — regardless of the score margin. Classic league table format, e.g. 3 pts for a win, 1 for a draw, 0 for a loss.',
  PLACEMENT:
    'Each finishing position pays out a fixed number of points. Set how many points 1st, 2nd, 3rd place… earns. Great for Mario Kart or any race-style format.',
  SCORE_BASED:
    'The raw match result (score, kills, goals) is converted directly into leaderboard points using a multiplier. A multiplier of 1 means 1 point per 1 unit of result. Use smaller values to scale down large scores.',
  WINNER_TAKES_ALL:
    'Only the match winner earns points — everyone else gets zero. High-stakes, clear-cut format. Good when draws cannot happen.',
  MANUAL:
    'Points are not calculated automatically. An admin enters leaderboard points by hand after each match. Maximum flexibility, zero automation.',
};

const ROTATION_RULE_DESC: Record<RotationRule, string> = {
  NONE:
    'No automatic rotation — admins pick who plays each match manually. Use this when you manage the schedule yourself.',
  CAPTAIN_CHOOSES:
    'A designated captain selects which players take part in each match. Good for team games where player selection is strategic.',
  RANDOM_PLAYER:
    'Players are picked at random for each match from the available pool. Keeps things unpredictable and fair.',
  LEAST_PLAYED_PLAYER:
    'The system always picks the player who has played the fewest matches so far. Automatically balances participation across the evening.',
  EVERYONE_MUST_PLAY:
    'Every player must play at least one match before anyone is allowed to play a second. Guarantees equal time on the controller.',
};

const BONUS_CONDITION_DESC: Record<BonusCondition, string> = {
  PERFECT_SCORE:
    'Awarded when a player or team achieves the maximum possible score, e.g. a 300 in Wii Bowling or a perfect run.',
  FASTEST_TIME:
    'Awarded to the player with the best (lowest) time in the session. Only one player earns this per session.',
  MOST_KILLS:
    'Awarded to the player with the highest kill or frag count in the session.',
  BIGGEST_WIN_MARGIN:
    'Awarded to the team that won by the largest margin — biggest score difference, most extra goals, etc.',
  NEW_RECORD:
    'Awarded when a player beats their own personal best or sets a new all-time record on this game.',
  CLEAN_SHEET:
    'Awarded when a team concedes zero goals, kills, or points during the entire match.',
};

const TIME_ACTION_DESC: Record<TimeAction, string> = {
  END_MATCH:
    'The match is immediately closed the moment the clock runs out. Whoever is leading at that instant wins.',
  USE_CURRENT_SCORE:
    'The score at time-up is locked in and treated as the final result, even if play was still in progress.',
  MANUAL_DECISION:
    'An admin decides what happens when time runs out — useful for games where overtime or penalty shootouts are common.',
};

// ─── Helper components ─────────────────────────────────────────────────────────

function Hint({ children }: { children: ReactNode }) {
  return <p className="mt-1.5 text-xs leading-relaxed text-slate-500">{children}</p>;
}

function ScoringRuleFields({
  rule,
  onChange,
}: {
  rule: ScoringRule;
  onChange: (r: ScoringRule) => void;
}) {
  switch (rule.type) {
    case 'WIN_DRAW_LOSS':
      return (
        <div className="space-y-3">
          <div className="grid grid-cols-3 gap-3">
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Win points</span>
              <input type="number" className={inputCls} value={rule.pointsForWin}
                onChange={(e) => onChange({ ...rule, pointsForWin: +e.target.value })} />
              <Hint>Points awarded to the winning team.</Hint>
            </label>
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Draw points</span>
              <input type="number" className={inputCls} value={rule.pointsForDraw}
                onChange={(e) => onChange({ ...rule, pointsForDraw: +e.target.value })} />
              <Hint>Points awarded to both teams when the result is a draw.</Hint>
            </label>
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Loss points</span>
              <input type="number" className={inputCls} value={rule.pointsForLoss}
                onChange={(e) => onChange({ ...rule, pointsForLoss: +e.target.value })} />
              <Hint>Points for the losing team. Usually 0, but can be positive to reward participation.</Hint>
            </label>
          </div>
        </div>
      );
    case 'PLACEMENT':
      return (
        <div className="space-y-2">
          <Hint>Set how many leaderboard points each finishing position is worth. Add or adjust rows to match the number of participants.</Hint>
          {Object.entries(rule.pointsByPlacement).sort(([a], [b]) => +a - +b).map(([place, pts]) => (
            <div key={place} className="flex items-center gap-3">
              <span className="w-20 text-xs font-medium text-slate-600">
                {place === '1' ? '🥇 1st' : place === '2' ? '🥈 2nd' : place === '3' ? '🥉 3rd' : `#${place}`}
              </span>
              <input type="number" className={`${inputCls} max-w-[120px]`} value={pts}
                onChange={(e) => onChange({
                  ...rule,
                  pointsByPlacement: { ...rule.pointsByPlacement, [+place]: +e.target.value },
                })} />
              <span className="text-xs text-slate-400">pts</span>
            </div>
          ))}
        </div>
      );
    case 'SCORE_BASED':
      return (
        <div className="grid grid-cols-2 gap-3">
          <label className="block">
            <span className="text-xs font-medium text-slate-600">Multiplier</span>
            <input type="number" step="0.01" className={inputCls} value={rule.multiplier}
              onChange={(e) => onChange({ ...rule, multiplier: +e.target.value })} />
            <Hint>Each unit of raw result is multiplied by this. Use 0.1 to convert a score of 300 into 30 leaderboard points.</Hint>
          </label>
          <label className="block">
            <span className="text-xs font-medium text-slate-600">Rounding</span>
            <select className={inputCls} value={rule.rounding}
              onChange={(e) => onChange({ ...rule, rounding: e.target.value as typeof rule.rounding })}>
              <option value="NONE">Truncate (drop decimals)</option>
              <option value="FLOOR">Floor (always round down)</option>
              <option value="CEIL">Ceiling (always round up)</option>
              <option value="ROUND">Round (standard ±0.5)</option>
            </select>
            <Hint>How to handle fractional points after applying the multiplier.</Hint>
          </label>
        </div>
      );
    case 'WINNER_TAKES_ALL':
      return (
        <label className="block">
          <span className="text-xs font-medium text-slate-600">Points to winner</span>
          <input type="number" className={`${inputCls} max-w-[160px]`} value={rule.pointsToWinner}
            onChange={(e) => onChange({ ...rule, pointsToWinner: +e.target.value })} />
          <Hint>The winner earns this many points. All other participants earn zero.</Hint>
        </label>
      );
    case 'MANUAL':
      return (
        <Hint>
          No automatic calculation. After each match an admin opens the results and types in
          each team's leaderboard points by hand.
        </Hint>
      );
  }
}

// ─── Styles ────────────────────────────────────────────────────────────────────

const inputCls =
  'mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100';
const selectCls = inputCls;
const sectionCls = 'space-y-4 rounded-lg border border-slate-200 bg-slate-50 p-4';
const legendCls = 'text-sm font-semibold text-slate-700';

// ─── Main form ─────────────────────────────────────────────────────────────────

export function GameForm({ initialValues, submitLabel, onSubmit }: GameFormProps) {
  const [values, setValues] = useState<GameFormValues>(initialValues ?? defaultValues);
  const [selectedPresetId, setSelectedPresetId] = useState<GameRulePresetId | ''>('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const selectedPreset = GAME_RULE_PRESETS.find((preset) => preset.id === selectedPresetId);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    if (!values.name.trim()) { setError('Game name is required'); return; }
    setSubmitting(true);
    try {
      await onSubmit(values);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save game');
    } finally {
      setSubmitting(false);
    }
  }

  function set<K extends keyof GameFormValues>(key: K, val: GameFormValues[K]) {
    setValues((v) => ({ ...v, [key]: val }));
  }

  function applySelectedPreset() {
    if (!selectedPreset) return;
    setValues((current) => ({
      ...current,
      genre: selectedPreset.values.genre,
      matchType: selectedPreset.values.matchType,
      participantRule: selectedPreset.values.participantRule,
      resultType: selectedPreset.values.resultType,
      winnerRule: selectedPreset.values.winnerRule,
      scoringRule: selectedPreset.values.scoringRule,
      tieBreakerRule: selectedPreset.values.tieBreakerRule,
      validationRule: selectedPreset.values.validationRule,
      rotationRule: selectedPreset.values.rotationRule,
      timeLimitRule: selectedPreset.values.timeLimitRule,
      bonusRules: selectedPreset.values.bonusRules,
    }));
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      {error ? <ErrorMessage message={error} /> : null}

      {/* ── Basic info ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>Basic information</h2>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">
            Game name <span className="text-red-500">*</span>
          </span>
          <input className={inputCls} maxLength={120} value={values.name}
            onChange={(e) => set('name', e.target.value)} required />
        </label>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">
            Description <span className="font-normal text-slate-500">(optional)</span>
          </span>
          <textarea className={inputCls} rows={2} value={values.description ?? ''}
            onChange={(e) => set('description', e.target.value)} />
          <Hint>Shown to players and admins wherever this game appears in a competition. Useful for noting special rules or setup instructions.</Hint>
        </label>

        <div className="grid grid-cols-2 gap-3">
          <label className="block">
            <span className="text-sm font-medium text-slate-700">
              Platform <span className="font-normal text-slate-500">(optional)</span>
            </span>
            <input className={inputCls} placeholder="e.g. Wii, PC, Switch"
              value={values.platform ?? ''}
              onChange={(e) => set('platform', e.target.value || null)} />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-slate-700">
              Genre <span className="font-normal text-slate-500">(optional)</span>
            </span>
            <input className={inputCls} placeholder="e.g. Racing, Sports, FPS"
              value={values.genre ?? ''}
              onChange={(e) => set('genre', e.target.value || null)} />
          </label>
        </div>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">
            Reference link <span className="font-normal text-slate-500">(optional)</span>
          </span>
          <input className={inputCls} type="url" placeholder="https://... (a rules site, a YouTube video, etc.)"
            value={values.referenceUrl ?? ''}
            onChange={(e) => set('referenceUrl', e.target.value || null)} />
          <Hint>Shown to players as a clickable link, e.g. a rules explainer or a how-to-play video. Must start with http:// or https://.</Hint>
        </label>

        <div>
          <label className="flex cursor-pointer items-center gap-2">
            <input type="checkbox" className="h-4 w-4 rounded accent-teal-700" checked={values.isActive}
              onChange={(e) => set('isActive', e.target.checked)} />
            <span className="text-sm font-medium text-slate-700">Active</span>
          </label>
          <Hint>Inactive games are hidden when building a new competition. Use this to retire a game without deleting its history.</Hint>
        </div>
      </div>

      {/* ── Optional setup helper ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>Game type preset</h2>
        <div className="grid gap-3 sm:grid-cols-[1fr_auto] sm:items-end">
          <label className="block">
            <span className="text-sm font-medium text-slate-700">
              Preset <span className="font-normal text-slate-500">(optional)</span>
            </span>
            <select className={selectCls} value={selectedPresetId}
              onChange={(e) => setSelectedPresetId(e.target.value as GameRulePresetId | '')}>
              <option value="">Choose a preset</option>
              {GAME_RULE_PRESETS.map((preset) => (
                <option key={preset.id} value={preset.id}>{preset.name}</option>
              ))}
            </select>
            <Hint>{selectedPreset?.summary ?? 'Use a preset to fill the rule sections below. You can still adjust every rule before saving.'}</Hint>
          </label>
          <button
            type="button"
            disabled={!selectedPreset}
            onClick={applySelectedPreset}
            className="inline-flex items-center justify-center gap-2 rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:border-teal-600 hover:text-teal-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Wand2 aria-hidden="true" className="h-4 w-4" />
            Apply preset
          </button>
        </div>
      </div>

      {/* ── Match setup ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>Match setup</h2>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Match type</span>
          <select className={selectCls} value={values.matchType}
            onChange={(e) => set('matchType', e.target.value as MatchType)}>
            <option value="PLAYER_VS_PLAYER">Player vs Player (1v1)</option>
            <option value="TEAM_VS_TEAM">Team vs Team</option>
            <option value="FREE_FOR_ALL">Free for All</option>
            <option value="SOLO_CHALLENGE">Solo Challenge</option>
            <option value="COOP_VS_AI">Co-op vs AI</option>
          </select>
          <Hint>{MATCH_TYPE_DESC[values.matchType]}</Hint>
        </label>

        <div>
          <p className="text-sm font-medium text-slate-700">Players per team</p>
          <div className="mt-1 grid grid-cols-2 gap-3">
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Minimum</span>
              <input type="number" min={1} className={inputCls}
                value={values.participantRule.minPlayersPerTeam}
                onChange={(e) => set('participantRule', { ...values.participantRule, minPlayersPerTeam: +e.target.value })} />
            </label>
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Maximum</span>
              <input type="number" min={1} className={inputCls}
                value={values.participantRule.maxPlayersPerTeam}
                onChange={(e) => set('participantRule', { ...values.participantRule, maxPlayersPerTeam: +e.target.value })} />
            </label>
          </div>
          <Hint>The allowed squad size per team. For a strict 1v1 set both to 1; for a flexible team game set min to 1 and max to however many can play at once.</Hint>
        </div>

        <div className="space-y-2">
          <div>
            <label className="flex cursor-pointer items-center gap-2">
              <input type="checkbox" className="h-4 w-4 rounded accent-teal-700"
                checked={values.participantRule.numberOfTeams === null}
                onChange={(e) => set('participantRule', {
                  ...values.participantRule,
                  numberOfTeams: e.target.checked ? null : 2,
                })} />
              <span className="text-sm font-medium text-slate-700">Any number of teams</span>
            </label>
            <Hint>Leave this on for Free for All games where the number of participants varies each session.</Hint>
          </div>
          {values.participantRule.numberOfTeams !== null && (
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Fixed number of teams</span>
              <input type="number" min={2} className={`${inputCls} max-w-[160px]`}
                value={values.participantRule.numberOfTeams}
                onChange={(e) => set('participantRule', { ...values.participantRule, numberOfTeams: +e.target.value })} />
              <Hint>Enforce an exact number of teams per match (e.g. 2 for a head-to-head game).</Hint>
            </label>
          )}
        </div>

        <div>
          <label className="flex cursor-pointer items-center gap-2">
            <input type="checkbox" className="h-4 w-4 rounded accent-teal-700"
              checked={values.participantRule.allowSubstitutes}
              onChange={(e) => set('participantRule', { ...values.participantRule, allowSubstitutes: e.target.checked })} />
            <span className="text-sm font-medium text-slate-700">Allow substitutes</span>
          </label>
          <Hint>When enabled, a player can be swapped in mid-match without counting as a full participant. Useful for long sessions where people rotate in and out.</Hint>
        </div>
      </div>

      {/* ── Result & winner rules ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>Result &amp; winner rules</h2>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Result type</span>
          <select className={selectCls} value={values.resultType}
            onChange={(e) => set('resultType', e.target.value as ResultType)}>
            <option value="SCORE">Score</option>
            <option value="TIME">Time</option>
            <option value="PLACEMENT">Placement (finishing position)</option>
            <option value="WINNER_ONLY">Winner only</option>
            <option value="KILLS">Kills / frags</option>
            <option value="GOALS">Goals</option>
            <option value="ROUNDS_WON">Rounds won</option>
            <option value="CUSTOM_NUMBER">Custom number</option>
          </select>
          <Hint>{RESULT_TYPE_DESC[values.resultType]}</Hint>
        </label>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Winner rule</span>
          <select className={selectCls} value={values.winnerRule}
            onChange={(e) => set('winnerRule', e.target.value as WinnerRule)}>
            <option value="HIGHEST_VALUE_WINS">Highest value wins</option>
            <option value="LOWEST_VALUE_WINS">Lowest value wins</option>
            <option value="FIRST_TO_FINISH_WINS">First to finish</option>
            <option value="LAST_REMAINING_WINS">Last remaining</option>
            <option value="MOST_ROUNDS_WON">Most rounds won</option>
            <option value="CLOSEST_TO_TARGET">Closest to target value</option>
            <option value="MANUAL_WINNER">Manual (admin decides)</option>
          </select>
          <Hint>{WINNER_RULE_DESC[values.winnerRule]}</Hint>
        </label>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Tie-breaker</span>
          <select className={selectCls} value={values.tieBreakerRule}
            onChange={(e) => set('tieBreakerRule', e.target.value as TieBreakerRule)}>
            <option value="ALLOW_DRAW">Allow draw</option>
            <option value="EXTRA_ROUND">Extra round</option>
            <option value="SUDDEN_DEATH">Sudden death</option>
            <option value="HIGHEST_SECONDARY_VALUE">Highest secondary value</option>
            <option value="LOWEST_SECONDARY_VALUE">Lowest secondary value</option>
            <option value="MANUAL_DECISION">Manual decision</option>
            <option value="RANDOM">Random (last resort)</option>
          </select>
          <Hint>{TIE_BREAKER_DESC[values.tieBreakerRule]}</Hint>
        </label>
      </div>

      {/* ── Scoring ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>Scoring</h2>
        <Hint>This controls how match outcomes are converted into leaderboard points for the overall competition standings.</Hint>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Scoring type</span>
          <select className={selectCls} value={values.scoringRule.type}
            onChange={(e) => set('scoringRule', defaultScoringRule(e.target.value as ScoringRule['type']))}>
            <option value="WIN_DRAW_LOSS">Win / Draw / Loss</option>
            <option value="PLACEMENT">Points by placement</option>
            <option value="SCORE_BASED">Score-based (result → points)</option>
            <option value="WINNER_TAKES_ALL">Winner takes all</option>
            <option value="MANUAL">Manual (admin assigns)</option>
          </select>
          <Hint>{SCORING_TYPE_DESC[values.scoringRule.type]}</Hint>
        </label>

        <ScoringRuleFields rule={values.scoringRule}
          onChange={(r) => set('scoringRule', r)} />
      </div>

      {/* ── Player rotation ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>Player rotation</h2>

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Rotation rule</span>
          <select className={selectCls} value={values.rotationRule ?? 'NONE'}
            onChange={(e) => set('rotationRule', e.target.value as RotationRule)}>
            <option value="NONE">None (manual)</option>
            <option value="CAPTAIN_CHOOSES">Captain chooses</option>
            <option value="RANDOM_PLAYER">Random player</option>
            <option value="LEAST_PLAYED_PLAYER">Least-played player first</option>
            <option value="EVERYONE_MUST_PLAY">Everyone must play before repeat</option>
          </select>
          <Hint>{ROTATION_RULE_DESC[values.rotationRule ?? 'NONE']}</Hint>
        </label>
      </div>

      {/* ── Result validation (optional) ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>
          Result validation <span className="font-normal text-slate-500">(optional)</span>
        </h2>
        <div>
          <label className="flex cursor-pointer items-center gap-2">
            <input type="checkbox" className="h-4 w-4 rounded accent-teal-700"
              checked={values.validationRule !== null}
              onChange={(e) => set('validationRule', e.target.checked
                ? { minValue: null, maxValue: null, allowDecimals: false, required: true }
                : null)} />
            <span className="text-sm font-medium text-slate-700">Enable result validation</span>
          </label>
          <Hint>Add guardrails so that implausible results cannot be accidentally entered — e.g. a bowling score above 300 or a negative time.</Hint>
        </div>

        {values.validationRule !== null && (
          <div className="space-y-4 pt-1">
            <div className="grid grid-cols-2 gap-3">
              <label className="block">
                <span className="text-xs font-medium text-slate-600">
                  Min value <span className="font-normal text-slate-400">(optional)</span>
                </span>
                <input type="number" step="any" className={inputCls}
                  value={values.validationRule.minValue ?? ''}
                  onChange={(e) => set('validationRule', {
                    ...values.validationRule!,
                    minValue: e.target.value === '' ? null : +e.target.value,
                  })} />
                <Hint>Results below this number will be rejected. Leave blank for no lower limit.</Hint>
              </label>
              <label className="block">
                <span className="text-xs font-medium text-slate-600">
                  Max value <span className="font-normal text-slate-400">(optional)</span>
                </span>
                <input type="number" step="any" className={inputCls}
                  value={values.validationRule.maxValue ?? ''}
                  onChange={(e) => set('validationRule', {
                    ...values.validationRule!,
                    maxValue: e.target.value === '' ? null : +e.target.value,
                  })} />
                <Hint>Results above this number will be rejected. Leave blank for no upper limit.</Hint>
              </label>
            </div>

            <div>
              <label className="flex cursor-pointer items-center gap-2">
                <input type="checkbox" className="h-4 w-4 rounded accent-teal-700"
                  checked={values.validationRule.allowDecimals}
                  onChange={(e) => set('validationRule', { ...values.validationRule!, allowDecimals: e.target.checked })} />
                <span className="text-sm font-medium text-slate-700">Allow decimal values</span>
              </label>
              <Hint>Turn on for times in seconds with milliseconds, or percentages. Keep off for whole-number results like goals or pins.</Hint>
            </div>

            <div>
              <label className="flex cursor-pointer items-center gap-2">
                <input type="checkbox" className="h-4 w-4 rounded accent-teal-700"
                  checked={values.validationRule.required}
                  onChange={(e) => set('validationRule', { ...values.validationRule!, required: e.target.checked })} />
                <span className="text-sm font-medium text-slate-700">Result is required</span>
              </label>
              <Hint>When checked, a match cannot be closed without a result being entered. Turn off for games where a result is sometimes unavailable.</Hint>
            </div>
          </div>
        )}
      </div>

      {/* ── Time limit (optional) ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>
          Time limit <span className="font-normal text-slate-500">(optional)</span>
        </h2>
        <div>
          <label className="flex cursor-pointer items-center gap-2">
            <input type="checkbox" className="h-4 w-4 rounded accent-teal-700"
              checked={values.timeLimitRule !== null}
              onChange={(e) => set('timeLimitRule', e.target.checked
                ? { hasTimeLimit: true, durationMinutes: 20, actionWhenTimeRunsOut: 'USE_CURRENT_SCORE' }
                : null)} />
            <span className="text-sm font-medium text-slate-700">Enable time limit</span>
          </label>
          <Hint>Set a maximum duration for each match. Useful for keeping the evening on schedule or for games that can run indefinitely.</Hint>
        </div>

        {values.timeLimitRule !== null && (
          <div className="space-y-4 pt-1">
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Duration (minutes)</span>
              <input type="number" min={1} className={`${inputCls} max-w-[160px]`}
                value={values.timeLimitRule.durationMinutes ?? ''}
                onChange={(e) => set('timeLimitRule', {
                  ...values.timeLimitRule!,
                  durationMinutes: e.target.value === '' ? null : +e.target.value,
                })} />
              <Hint>How long each match is allowed to run before time is called.</Hint>
            </label>

            <label className="block">
              <span className="text-xs font-medium text-slate-600">When time runs out</span>
              <select className={`${selectCls} max-w-xs`} value={values.timeLimitRule.actionWhenTimeRunsOut}
                onChange={(e) => set('timeLimitRule', {
                  ...values.timeLimitRule!,
                  actionWhenTimeRunsOut: e.target.value as TimeAction,
                })}>
                <option value="END_MATCH">End match immediately</option>
                <option value="USE_CURRENT_SCORE">Use current score</option>
                <option value="MANUAL_DECISION">Manual decision</option>
              </select>
              <Hint>{TIME_ACTION_DESC[values.timeLimitRule.actionWhenTimeRunsOut]}</Hint>
            </label>
          </div>
        )}
      </div>

      {/* ── Bonus rules (optional) ── */}
      <div className={sectionCls}>
        <h2 className={legendCls}>
          Bonus rules <span className="font-normal text-slate-500">(optional)</span>
        </h2>
        <Hint>Award extra leaderboard points for exceptional individual achievements within a match. These stack on top of the regular scoring result.</Hint>

        {values.bonusRules.map((bonus, i) => (
          <div key={i} className="rounded-md border border-slate-200 bg-white p-3 space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <label className="block">
                <span className="text-xs font-medium text-slate-600">Name</span>
                <input className={inputCls} placeholder="e.g. Perfect game"
                  value={bonus.name}
                  onChange={(e) => {
                    const updated = [...values.bonusRules];
                    updated[i] = { ...bonus, name: e.target.value };
                    set('bonusRules', updated);
                  }} />
              </label>
              <label className="block">
                <span className="text-xs font-medium text-slate-600">Bonus points</span>
                <input type="number" className={inputCls} value={bonus.bonusPoints}
                  onChange={(e) => {
                    const updated = [...values.bonusRules];
                    updated[i] = { ...bonus, bonusPoints: +e.target.value };
                    set('bonusRules', updated);
                  }} />
              </label>
            </div>
            <label className="block">
              <span className="text-xs font-medium text-slate-600">Trigger condition</span>
              <select className={selectCls} value={bonus.condition}
                onChange={(e) => {
                  const updated = [...values.bonusRules];
                  updated[i] = { ...bonus, condition: e.target.value as BonusCondition };
                  set('bonusRules', updated);
                }}>
                <option value="PERFECT_SCORE">Perfect score</option>
                <option value="FASTEST_TIME">Fastest time</option>
                <option value="MOST_KILLS">Most kills / frags</option>
                <option value="BIGGEST_WIN_MARGIN">Biggest win margin</option>
                <option value="NEW_RECORD">New personal record</option>
                <option value="CLEAN_SHEET">Clean sheet (zero conceded)</option>
              </select>
              <Hint>{BONUS_CONDITION_DESC[bonus.condition]}</Hint>
            </label>
            <button type="button"
              onClick={() => set('bonusRules', values.bonusRules.filter((_, j) => j !== i))}
              className="text-xs text-red-600 hover:underline">
              Remove this bonus rule
            </button>
          </div>
        ))}

        <button type="button"
          onClick={() => set('bonusRules', [...values.bonusRules, { name: '', condition: 'PERFECT_SCORE', bonusPoints: 1 }])}
          className="rounded-md border border-dashed border-slate-300 px-3 py-2 text-sm text-slate-500 hover:border-teal-400 hover:text-teal-700">
          + Add bonus rule
        </button>
      </div>

      <button
        type="submit"
        disabled={submitting}
        className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <Save aria-hidden="true" className="h-4 w-4" />
        {submitting ? 'Saving...' : submitLabel}
      </button>
    </form>
  );
}
