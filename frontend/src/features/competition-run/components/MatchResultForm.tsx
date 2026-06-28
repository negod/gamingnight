import { Save, X } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import type { Match, PlayerResult } from '../../../shared/types/match';
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
  onSave: (results: PlayerResult[]) => Promise<void>;
  onCancel: () => void;
};

export function MatchResultForm({ match, onSave, onCancel }: MatchResultFormProps) {
  const [rows, setRows] = useState<PlayerRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);

    Promise.all([getTeam(match.homeTeamId), getTeam(match.awayTeamId)])
      .then(async ([homeTeam, awayTeam]: [Team, Team]) => {
        const allPlayerIds = [...homeTeam.playerIds.map((id: string) => ({ id, team: homeTeam })),
                             ...awayTeam.playerIds.map((id: string) => ({ id, team: awayTeam }))];

        const players = await Promise.all(
          allPlayerIds.map(({ id }) => getPlayer(id).catch((): Player => ({
            id,
            name: id,
            createdAt: '',
            updatedAt: '',
          }))),
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

  return (
    <div className="rounded-lg border border-teal-200 bg-teal-50 p-4">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="font-semibold text-slate-900">
          {match.homeTeamName} vs {match.awayTeamName}
        </h3>
        <button
          type="button"
          onClick={onCancel}
          className="rounded p-1 text-slate-500 hover:bg-slate-200"
          aria-label="Cancel"
        >
          <X className="h-4 w-4" aria-hidden="true" />
        </button>
      </div>

      {error ? <ErrorMessage message={error} /> : null}

      <form onSubmit={handleSubmit} noValidate className="space-y-2">
        {rows.map((row, i) => (
          <div key={row.playerId} className="flex items-center gap-3">
            <span className="w-32 truncate text-sm text-slate-600">{row.teamName}</span>
            <span className="flex-1 truncate text-sm font-medium text-slate-800">{row.playerName}</span>
            <input
              type="number"
              step="any"
              value={row.value}
              onChange={(e) => {
                const next = [...rows];
                next[i] = { ...next[i], value: e.target.value };
                setRows(next);
              }}
              className="w-24 rounded-md border border-slate-300 px-2 py-1 text-right text-sm outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
              placeholder="0"
            />
          </div>
        ))}

        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={onCancel}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="inline-flex items-center gap-1.5 rounded-md bg-teal-700 px-3 py-1.5 text-sm font-semibold text-white hover:bg-teal-800 disabled:opacity-60"
          >
            <Save className="h-3.5 w-3.5" aria-hidden="true" />
            {submitting ? 'Saving…' : 'Save results'}
          </button>
        </div>
      </form>
    </div>
  );
}
