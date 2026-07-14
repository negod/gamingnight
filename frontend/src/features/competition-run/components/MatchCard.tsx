import { Crown, Minus, Pencil } from 'lucide-react';
import type { Match, PlayerResult } from '../../../shared/types/match';
import type { Game } from '../../../shared/types/game';
import { formatResultValue, roundResultValue } from './formatResultValue';

type MatchCardProps = {
  match: Match;
  game?: Game;
  onEdit?: (match: Match) => void;
};

// ─── Winner resolution ────────────────────────────────────────────────────────

function teamTotal(results: PlayerResult[], teamId: string): number {
  return roundResultValue(results.filter((r) => r.teamId === teamId).reduce((s, r) => s + r.value, 0));
}

type WinnerOutcome = { kind: 'home' | 'away' | 'draw' } | { kind: 'unknown' };

function resolveWinner(match: Match, game: Game | undefined): WinnerOutcome {
  if (!match.completed || match.results.length === 0 || !game) return { kind: 'unknown' };
  const home = teamTotal(match.results, match.homeTeamId);
  const away = teamTotal(match.results, match.awayTeamId);
  const rule = game.winnerRule;
  if (rule === 'HIGHEST_VALUE_WINS' || rule === 'MOST_ROUNDS_WON' || rule === 'LAST_REMAINING_WINS') {
    if (home === away) return { kind: 'draw' };
    return { kind: home > away ? 'home' : 'away' };
  }
  if (rule === 'LOWEST_VALUE_WINS') {
    if (home === away) return { kind: 'draw' };
    return { kind: home < away ? 'home' : 'away' };
  }
  return { kind: 'unknown' }; // MANUAL, CLOSEST_TO_TARGET, FIRST_TO_FINISH, etc.
}

// ─── Status badge ─────────────────────────────────────────────────────────────

function StatusBadge({ match }: { match: Match }) {
  return match.completed ? (
    <span className="rounded-full bg-teal-100 px-2 py-0.5 text-xs font-medium text-teal-800">Done</span>
  ) : (
    <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800">Pending</span>
  );
}

// ─── Edit button ──────────────────────────────────────────────────────────────

function EditBtn({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="rounded-md border border-slate-300 p-1.5 text-slate-600 hover:bg-slate-100"
      aria-label={label}
    >
      <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
    </button>
  );
}

// ─── PLAYER_VS_PLAYER — duel card ─────────────────────────────────────────────

function DuelCard({ match, game, onEdit }: { match: Match; game?: Game; onEdit?: (m: Match) => void }) {
  const homeResult = match.results.find((r) => r.teamId === match.homeTeamId);
  const awayResult = match.results.find((r) => r.teamId === match.awayTeamId);
  const outcome = resolveWinner(match, game);
  const homeWon = outcome.kind === 'home';
  const awayWon = outcome.kind === 'away';
  const draw = outcome.kind === 'draw';

  const homeName = homeResult?.playerName ?? match.homeTeamName;
  const awayName = awayResult?.playerName ?? match.awayTeamName;

  function sideClass(won: boolean): string {
    if (!match.completed) return 'bg-slate-50';
    if (won) return 'bg-teal-50 ring-2 ring-teal-400';
    if (draw) return 'bg-slate-50';
    return 'bg-slate-50 opacity-60';
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="flex items-center gap-2">

        {/* Home side */}
        <div className={`flex flex-1 flex-col items-center gap-1 rounded-lg px-3 py-3 ${sideClass(homeWon)}`}>
          <div className="flex items-center gap-1">
            {homeWon && <Crown className="h-3.5 w-3.5 shrink-0 text-teal-600" aria-hidden="true" />}
            <span className={`text-sm font-semibold leading-tight text-center ${homeWon ? 'text-teal-800' : 'text-slate-800'}`}>
              {homeName}
            </span>
          </div>
          {homeName !== match.homeTeamName && (
            <span className="text-xs text-slate-400">{match.homeTeamName}</span>
          )}
          {match.completed && homeResult != null ? (
            <span className={`mt-1 text-2xl font-bold tabular-nums ${homeWon ? 'text-teal-700' : 'text-slate-400'}`}>
              {formatResultValue(homeResult.value)}
            </span>
          ) : (
            <span className="mt-1 text-2xl font-bold text-slate-200">–</span>
          )}
        </div>

        {/* Centre */}
        <div className="flex w-12 flex-col items-center gap-1">
          {draw ? (
            <>
              <Minus className="h-4 w-4 text-slate-400" aria-hidden="true" />
              <span className="text-xs font-semibold text-slate-500">Draw</span>
            </>
          ) : (
            <>
              <span className="text-xs font-bold text-slate-400">VS</span>
              {!match.completed && <span className="text-xs text-amber-600">Pending</span>}
            </>
          )}
        </div>

        {/* Away side */}
        <div className={`flex flex-1 flex-col items-center gap-1 rounded-lg px-3 py-3 ${sideClass(awayWon)}`}>
          <div className="flex items-center gap-1">
            {awayWon && <Crown className="h-3.5 w-3.5 shrink-0 text-teal-600" aria-hidden="true" />}
            <span className={`text-sm font-semibold leading-tight text-center ${awayWon ? 'text-teal-800' : 'text-slate-800'}`}>
              {awayName}
            </span>
          </div>
          {awayName !== match.awayTeamName && (
            <span className="text-xs text-slate-400">{match.awayTeamName}</span>
          )}
          {match.completed && awayResult != null ? (
            <span className={`mt-1 text-2xl font-bold tabular-nums ${awayWon ? 'text-teal-700' : 'text-slate-400'}`}>
              {formatResultValue(awayResult.value)}
            </span>
          ) : (
            <span className="mt-1 text-2xl font-bold text-slate-200">–</span>
          )}
        </div>

        {/* Actions */}
        <div className="flex flex-col items-end gap-1">
          <StatusBadge match={match} />
          {onEdit && <EditBtn label={`Enter results for ${homeName} vs ${awayName}`} onClick={() => onEdit(match)} />}
        </div>
      </div>
    </div>
  );
}

// ─── TEAM_VS_TEAM / FREE_FOR_ALL — team card ──────────────────────────────────

function TeamCard({ match, game, onEdit }: { match: Match; game?: Game; onEdit?: (m: Match) => void }) {
  const outcome = resolveWinner(match, game);
  const homeWon = outcome.kind === 'home';
  const awayWon = outcome.kind === 'away';
  const draw = outcome.kind === 'draw';

  const homeResults = match.results.filter((r) => r.teamId === match.homeTeamId);
  const awayResults = match.results.filter((r) => r.teamId === match.awayTeamId);
  const homeTotal = teamTotal(match.results, match.homeTeamId);
  const awayTotal = teamTotal(match.results, match.awayTeamId);

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 space-y-0">

      {/* Team header */}
      <div className="flex items-center justify-between gap-3">
        {/* Home team */}
        <div className="flex flex-1 items-center gap-1.5 min-w-0">
          {homeWon && <Crown className="h-3.5 w-3.5 shrink-0 text-teal-600" aria-hidden="true" />}
          <span className={`truncate font-semibold ${homeWon ? 'text-teal-800' : 'text-slate-900'}`}>
            {match.homeTeamName}
          </span>
        </div>

        {/* Score / status */}
        <div className="flex shrink-0 flex-col items-center">
          {match.completed ? (
            <span className={`text-sm font-bold tabular-nums ${draw ? 'text-slate-500' : 'text-slate-800'}`}>
              {formatResultValue(homeTotal)} – {formatResultValue(awayTotal)}
            </span>
          ) : (
            <span className="text-xs font-bold text-slate-400">VS</span>
          )}
          {draw && <span className="text-xs font-semibold text-slate-500">Draw</span>}
        </div>

        {/* Away team */}
        <div className="flex flex-1 items-center justify-end gap-1.5 min-w-0">
          <span className={`truncate font-semibold ${awayWon ? 'text-teal-800' : 'text-slate-900'}`}>
            {match.awayTeamName}
          </span>
          {awayWon && <Crown className="h-3.5 w-3.5 shrink-0 text-teal-600" aria-hidden="true" />}
        </div>

        {/* Status + edit */}
        <div className="flex shrink-0 items-center gap-2">
          <StatusBadge match={match} />
          {onEdit && (
            <EditBtn
              label={`Enter results for ${match.homeTeamName} vs ${match.awayTeamName}`}
              onClick={() => onEdit(match)}
            />
          )}
        </div>
      </div>

      {/* Per-player results grouped by team */}
      {match.completed && match.results.length > 0 && (
        <div className="mt-3 grid grid-cols-2 gap-x-4 border-t border-slate-100 pt-3">
          <ul className="space-y-1">
            {homeResults.map((r, i) => (
              <li key={i} className="flex justify-between text-sm">
                <span className="font-medium text-slate-800 truncate">{r.playerName}</span>
                <span className="ml-2 shrink-0 font-mono text-slate-600">{formatResultValue(r.value)}</span>
              </li>
            ))}
          </ul>
          <ul className="space-y-1">
            {awayResults.map((r, i) => (
              <li key={i} className="flex justify-between text-sm">
                <span className="font-medium text-slate-800 truncate">{r.playerName}</span>
                <span className="ml-2 shrink-0 font-mono text-slate-600">{formatResultValue(r.value)}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

// ─── Export ───────────────────────────────────────────────────────────────────

export function MatchCard({ match, game, onEdit }: MatchCardProps) {
  if (game?.matchType === 'PLAYER_VS_PLAYER') {
    return <DuelCard match={match} game={game} onEdit={onEdit} />;
  }
  return <TeamCard match={match} game={game} onEdit={onEdit} />;
}
