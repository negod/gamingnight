import { Save } from 'lucide-react';
import { FormEvent, useState } from 'react';
import type { CompetitionFormValues } from '../../../shared/types/competition';
import type { Game } from '../../../shared/types/game';
import type { Team } from '../../../shared/types/team';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';

type CompetitionFormProps = {
  initialValues?: CompetitionFormValues;
  games: Game[];
  teams: Team[];
  submitLabel: string;
  onSubmit: (values: CompetitionFormValues) => Promise<void>;
};

export function CompetitionForm({ initialValues, games, teams, submitLabel, onSubmit }: CompetitionFormProps) {
  const [values, setValues] = useState<CompetitionFormValues>(
    initialValues ?? { name: '', date: '', singleMatch: true, gameIds: [], teamIds: [] },
  );
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function toggleGame(gameId: string) {
    setValues((prev) => ({
      ...prev,
      gameIds: prev.gameIds.includes(gameId)
        ? prev.gameIds.filter((id) => id !== gameId)
        : [...prev.gameIds, gameId],
    }));
  }

  function toggleTeam(teamId: string) {
    setValues((prev) => ({
      ...prev,
      teamIds: prev.teamIds.includes(teamId)
        ? prev.teamIds.filter((id) => id !== teamId)
        : [...prev.teamIds, teamId],
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!values.name.trim()) {
      setError('Competition name is required');
      return;
    }
    if (!values.date) {
      setError('Date is required');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit(values);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save competition');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      {error ? <ErrorMessage message={error} /> : null}

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Competition name</span>
        <input
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={120}
          value={values.name}
          onChange={(e) => setValues({ ...values, name: e.target.value })}
          required
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Date</span>
        <input
          type="date"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          value={values.date}
          onChange={(e) => setValues({ ...values, date: e.target.value })}
          required
        />
      </label>

      <label className="flex cursor-pointer items-center gap-3">
        <input
          type="checkbox"
          checked={values.singleMatch}
          onChange={(e) => setValues({ ...values, singleMatch: e.target.checked })}
          className="h-4 w-4 accent-teal-700"
        />
        <span className="text-sm font-medium text-slate-700">Single match per team pair</span>
      </label>

      <fieldset>
        <legend className="text-sm font-medium text-slate-700">
          Games <span className="font-normal text-slate-500">(selection order determines play order)</span>
        </legend>
        {games.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500">No games available.</p>
        ) : (
          <div className="mt-2 grid gap-2 sm:grid-cols-2">
            {games.map((game) => {
              const order = values.gameIds.indexOf(game.id);
              return (
                <label key={game.id} className="flex cursor-pointer items-center gap-2 rounded-md border border-slate-200 px-3 py-2 hover:bg-slate-50">
                  <input
                    type="checkbox"
                    checked={values.gameIds.includes(game.id)}
                    onChange={() => toggleGame(game.id)}
                    className="h-4 w-4 accent-teal-700"
                  />
                  {order >= 0 && (
                    <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-teal-700 text-xs font-bold text-white">
                      {order + 1}
                    </span>
                  )}
                  <span className="text-sm text-slate-800">{game.name}</span>
                </label>
              );
            })}
          </div>
        )}
      </fieldset>

      <fieldset>
        <legend className="text-sm font-medium text-slate-700">Teams</legend>
        {teams.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500">No teams available. Create teams first or use the auto-generate wizard on the edit page.</p>
        ) : (
          <div className="mt-2 grid gap-2 sm:grid-cols-2">
            {teams.map((team) => (
              <label key={team.id} className="flex cursor-pointer items-center gap-2 rounded-md border border-slate-200 px-3 py-2 hover:bg-slate-50">
                <input
                  type="checkbox"
                  checked={values.teamIds.includes(team.id)}
                  onChange={() => toggleTeam(team.id)}
                  className="h-4 w-4 accent-teal-700"
                />
                <span className="text-sm text-slate-800">
                  {team.name}
                  <span className="ml-1 text-slate-400">({team.playerIds.length})</span>
                </span>
              </label>
            ))}
          </div>
        )}
      </fieldset>

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
