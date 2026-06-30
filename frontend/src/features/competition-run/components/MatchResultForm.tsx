import { Save, X } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import type { Match, PlayerResult } from '../../../shared/types/match';
import type { Game } from '../../../shared/types/game';
import type { Team } from '../../../shared/types/team';
import type { Player } from '../../../shared/types/player';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import { getTeam, getPlayer } from '../api/competitionRunApi';

type PlayerRow = {
  playerId: string;
  teamId: string;
  playerName: string;
  teamName: string;
  value: string;
};

type MatchResultFormProps = {
  match: Match;
  game?: Game;
  onSave: (results: PlayerResult[]) => Promise<void>;
  onCancel: () => void;
};

// ─── Shared input ─────────────────────────────────────────────────────────────

const inputCls =
  'w-24 rounded-md border border-slate-300 px-2 py-1.5 text-right text-sm tabular-nums outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100';

// ─── PLAYER_VS_PLAYER — side-by-side duel form ────────────────────────────────

function DuelResultForm({
  match,
  rows,
  onChange,
  onSubmit,
  onCancel,
  submitting,
  error,
}: {
  match: Match;
  rows: PlayerRow[];
  onChange: (index: number, value: string) => void;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
  onCancel: () => void;
  submitting: boolean;
  error: string | null;
}) {
  const homeRow = rows.find((r) => r.teamId === match.homeTeamId);
  const awayRow = rows.find((r) => r.teamId === match.awayTeamId);
  const homeIndex = rows.findIndex((r) => r.teamId === match.homeTeamId);
  const awayIndex = rows.findIndex((r) => r.teamId === match.awayTeamId);

  return (
    <div className="rounded-lg border border-teal-200 bg-teal-50 p-4 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-slate-900">Enter results</h3>
        <button type="button" onClick={onCancel}
          className="rounded p-1 text-slate-500 hover:bg-slate-200" aria-label="Cancel">
          <X className="h-4 w-4" aria-hidden="true" />
        </button>
      </div>

      {error ? <ErrorMessage message={error} /> : null}

      <form onSubmit={onSubmit} noValidate>
        {/* Duel layout */}
        <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-3">

          {/* Home side */}
          <div className="flex flex-col items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-4 text-center">
            <span className="text-sm font-semibold text-slate-900">
              {homeRow?.playerName ?? match.homeTeamName}
            </span>
            {homeRow && homeRow.playerName !== match.homeTeamName && (
              <span className="text-xs text-slate-400">{match.homeTeamName}</span>
            )}
            <input
              type="number"
              step="any"
              value={homeRow?.value ?? ''}
              onChange={(e) => homeIndex >= 0 && onChange(homeIndex, e.target.value)}
              className={`${inputCls} w-full text-center text-xl font-bold`}
              placeholder="0"
              aria-label={`Score for ${homeRow?.playerName ?? match.homeTeamName}`}
            />
          </div>

          {/* VS */}
          <div className="flex flex-col items-center">
            <span className="text-xs font-bold text-slate-400">VS</span>
          </div>

          {/* Away side */}
          <div className="flex flex-col items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-4 text-center">
            <span className="text-sm font-semibold text-slate-900">
              {awayRow?.playerName ?? match.awayTeamName}
            </span>
            {awayRow && awayRow.playerName !== match.awayTeamName && (
              <span className="text-xs text-slate-400">{match.awayTeamName}</span>
            )}
            <input
              type="number"
              step="any"
              value={awayRow?.value ?? ''}
              onChange={(e) => awayIndex >= 0 && onChange(awayIndex, e.target.value)}
              className={`${inputCls} w-full text-center text-xl font-bold`}
              placeholder="0"
              aria-label={`Score for ${awayRow?.playerName ?? match.awayTeamName}`}
            />
          </div>
        </div>

        <div className="flex justify-end gap-2 pt-4">
          <button type="button" onClick={onCancel}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100">
            Cancel
          </button>
          <button type="submit" disabled={submitting}
            className="inline-flex items-center gap-1.5 rounded-md bg-teal-700 px-3 py-1.5 text-sm font-semibold text-white hover:bg-teal-800 disabled:opacity-60">
            <Save className="h-3.5 w-3.5" aria-hidden="true" />
            {submitting ? 'Saving…' : 'Save results'}
          </button>
        </div>
      </form>
    </div>
  );
}

// ─── TEAM_VS_TEAM / FREE_FOR_ALL — grouped form ───────────────────────────────

function GroupedResultForm({
  match,
  rows,
  onChange,
  onSubmit,
  onCancel,
  submitting,
  error,
}: {
  match: Match;
  rows: PlayerRow[];
  onChange: (index: number, value: string) => void;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
  onCancel: () => void;
  submitting: boolean;
  error: string | null;
}) {
  const homeRows = rows
    .map((r, i) => ({ ...r, index: i }))
    .filter((r) => r.teamId === match.homeTeamId);
  const awayRows = rows
    .map((r, i) => ({ ...r, index: i }))
    .filter((r) => r.teamId === match.awayTeamId);

  return (
    <div className="rounded-lg border border-teal-200 bg-teal-50 p-4 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-slate-900">
          {match.homeTeamName} <span className="text-slate-400">vs</span> {match.awayTeamName}
        </h3>
        <button type="button" onClick={onCancel}
          className="rounded p-1 text-slate-500 hover:bg-slate-200" aria-label="Cancel">
          <X className="h-4 w-4" aria-hidden="true" />
        </button>
      </div>

      {error ? <ErrorMessage message={error} /> : null}

      <form onSubmit={onSubmit} noValidate className="space-y-4">
        {/* Two-column grid: home left, away right */}
        <div className="grid grid-cols-2 gap-4">
          {/* Home team */}
          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {match.homeTeamName}
            </p>
            {homeRows.map((row) => (
              <div key={row.playerId} className="flex items-center justify-between gap-2">
                <span className="min-w-0 flex-1 truncate text-sm font-medium text-slate-800">
                  {row.playerName}
                </span>
                <input
                  type="number"
                  step="any"
                  value={row.value}
                  onChange={(e) => onChange(row.index, e.target.value)}
                  className={inputCls}
                  placeholder="0"
                  aria-label={`Score for ${row.playerName}`}
                />
              </div>
            ))}
          </div>

          {/* Away team */}
          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {match.awayTeamName}
            </p>
            {awayRows.map((row) => (
              <div key={row.playerId} className="flex items-center justify-between gap-2">
                <span className="min-w-0 flex-1 truncate text-sm font-medium text-slate-800">
                  {row.playerName}
                </span>
                <input
                  type="number"
                  step="any"
                  value={row.value}
                  onChange={(e) => onChange(row.index, e.target.value)}
                  className={inputCls}
                  placeholder="0"
                  aria-label={`Score for ${row.playerName}`}
                />
              </div>
            ))}
          </div>
        </div>

        <div className="flex justify-end gap-2">
          <button type="button" onClick={onCancel}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100">
            Cancel
          </button>
          <button type="submit" disabled={submitting}
            className="inline-flex items-center gap-1.5 rounded-md bg-teal-700 px-3 py-1.5 text-sm font-semibold text-white hover:bg-teal-800 disabled:opacity-60">
            <Save className="h-3.5 w-3.5" aria-hidden="true" />
            {submitting ? 'Saving…' : 'Save results'}
          </button>
        </div>
      </form>
    </div>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

export function MatchResultForm({ match, game, onSave, onCancel }: MatchResultFormProps) {
  const [rows, setRows] = useState<PlayerRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);

    Promise.all([getTeam(match.homeTeamId), getTeam(match.awayTeamId)])
      .then(async ([homeTeam, awayTeam]: [Team, Team]) => {
        const allPlayerIds = [
          ...homeTeam.playerIds.map((id: string) => ({ id, team: homeTeam })),
          ...awayTeam.playerIds.map((id: string) => ({ id, team: awayTeam })),
        ];

        const players = await Promise.all(
          allPlayerIds.map(({ id }) =>
            getPlayer(id).catch((): Player => ({ id, name: id, createdAt: '', updatedAt: '' })),
          ),
        );

        if (!active) return;

        const existingByPlayer = new Map(match.results.map((r) => [r.playerId, r.value]));

        const builtRows: PlayerRow[] = allPlayerIds.map(({ id, team }, index) => ({
          playerId: id,
          teamId: team.id,
          playerName: players[index].name,
          teamName: team.name,
          value: String(existingByPlayer.get(id) ?? ''),
        }));

        setRows(builtRows);
        setLoading(false);
      })
      .catch((err: unknown) => {
        if (active) {
          setError(err instanceof Error ? err.message : 'Failed to load teams');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [match.awayTeamId, match.homeTeamId, match.id, match.results]);

  function handleChange(index: number, value: string) {
    setRows((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], value };
      return next;
    });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const results: PlayerResult[] = rows.map((r) => ({
        playerId: r.playerId,
        teamId: r.teamId,
        playerName: r.playerName,
        teamName: r.teamName,
        value: parseFloat(r.value) || 0,
      }));
      await onSave(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save results');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <p className="text-sm text-slate-500">Loading players…</p>;

  const sharedProps = { match, rows, onChange: handleChange, onSubmit: handleSubmit, onCancel, submitting, error };

  if (game?.matchType === 'PLAYER_VS_PLAYER') {
    return <DuelResultForm {...sharedProps} />;
  }
  return <GroupedResultForm {...sharedProps} />;
}
