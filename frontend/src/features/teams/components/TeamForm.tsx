import { Save } from 'lucide-react';
import { FormEvent, useState } from 'react';
import type { Player } from '../../../shared/types/player';
import type { TeamFormValues } from '../../../shared/types/team';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';

type TeamFormProps = {
  initialValues?: TeamFormValues;
  players: Player[];
  submitLabel: string;
  onSubmit: (values: TeamFormValues) => Promise<void>;
};

export function TeamForm({ initialValues, players, submitLabel, onSubmit }: TeamFormProps) {
  const [values, setValues] = useState<TeamFormValues>(
    initialValues ?? { name: '', playerIds: [] },
  );
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function togglePlayer(playerId: string) {
    setValues((prev) => ({
      ...prev,
      playerIds: prev.playerIds.includes(playerId)
        ? prev.playerIds.filter((id) => id !== playerId)
        : [...prev.playerIds, playerId],
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!values.name.trim()) {
      setError('Team name is required');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit(values);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save team');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      {error ? <ErrorMessage message={error} /> : null}

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Team name</span>
        <input
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={120}
          value={values.name}
          onChange={(e) => setValues({ ...values, name: e.target.value })}
          required
        />
      </label>

      <fieldset>
        <legend className="text-sm font-medium text-slate-700">Players</legend>
        {players.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500">No players available.</p>
        ) : (
          <div className="mt-2 grid gap-2 sm:grid-cols-2">
            {players.map((player) => (
              <label key={player.id} className="flex cursor-pointer items-center gap-2 rounded-md border border-slate-200 px-3 py-2 hover:bg-slate-50">
                <input
                  type="checkbox"
                  checked={values.playerIds.includes(player.id)}
                  onChange={() => togglePlayer(player.id)}
                  className="h-4 w-4 accent-teal-700"
                />
                <span className="text-sm text-slate-800">{player.name}</span>
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
