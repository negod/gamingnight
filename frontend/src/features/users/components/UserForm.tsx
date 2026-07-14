import { FormEvent, useState } from 'react';
import { Save } from 'lucide-react';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import type { Player } from '../../../shared/types/player';
import type { UserFormValues, UserRole } from '../../../shared/types/user';

type PlayerMode = 'existing' | 'new';

type UserFormProps = {
  players: Player[];
  initialValues?: UserFormValues;
  submitLabel: string;
  onSubmit: (values: UserFormValues) => Promise<void>;
};

const roles: Array<{ value: UserRole; label: string }> = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'USER', label: 'User' },
];

export function UserForm({ players, initialValues, submitLabel, onSubmit }: UserFormProps) {
  const [values, setValues] = useState<UserFormValues>(
    initialValues ?? { username: '', password: '', role: 'USER', playerId: '', email: '' },
  );
  const [playerMode, setPlayerMode] = useState<PlayerMode>('existing');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const creating = !initialValues;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!values.username.trim()) {
      setError('Username is required');
      return;
    }
    if (!initialValues && !values.password?.trim()) {
      setError('Password is required');
      return;
    }
    if (playerMode === 'existing' && !values.playerId) {
      setError('Player callsign is required');
      return;
    }
    if (playerMode === 'new' && !values.playerCallsign?.trim()) {
      setError('Player callsign is required');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit({
        username: values.username.trim(),
        email: values.email ?? '',
        password: values.password,
        role: values.role,
        playerId: playerMode === 'existing' ? values.playerId : undefined,
        playerCallsign: playerMode === 'new' ? values.playerCallsign?.trim() : undefined,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save user');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      {error ? <ErrorMessage message={error} /> : null}

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Username</span>
        <input
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={120}
          value={values.username}
          onChange={(e) => setValues((current) => ({ ...current, username: e.target.value }))}
          required
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Email</span>
        <input
          type="email"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={320}
          value={values.email ?? ''}
          onChange={(e) => setValues((current) => ({ ...current, email: e.target.value }))}
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Role</span>
        <select
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          value={values.role}
          onChange={(e) => setValues((current) => ({ ...current, role: e.target.value as UserRole }))}
        >
          {roles.map((role) => (
            <option key={role.value} value={role.value}>
              {role.label}
            </option>
          ))}
        </select>
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">
          {initialValues ? 'New password' : 'Password'}
        </span>
        <input
          type="password"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          value={values.password ?? ''}
          onChange={(e) => setValues((current) => ({ ...current, password: e.target.value }))}
          minLength={4}
          maxLength={200}
          required={!initialValues}
        />
        {initialValues ? (
          <span className="mt-1 block text-xs text-slate-500">Leave blank to keep the current password.</span>
        ) : null}
      </label>

      {creating ? (
        <fieldset className="space-y-3">
          <legend className="text-sm font-medium text-slate-700">Player callsign</legend>
          <div className="flex flex-wrap gap-4">
            <label className="inline-flex items-center gap-2 text-sm text-slate-700">
              <input
                type="radio"
                name="playerMode"
                value="existing"
                checked={playerMode === 'existing'}
                onChange={() => setPlayerMode('existing')}
                className="h-4 w-4 border-slate-300 text-teal-700 focus:ring-teal-700"
              />
              Existing
            </label>
            <label className="inline-flex items-center gap-2 text-sm text-slate-700">
              <input
                type="radio"
                name="playerMode"
                value="new"
                checked={playerMode === 'new'}
                onChange={() => setPlayerMode('new')}
                className="h-4 w-4 border-slate-300 text-teal-700 focus:ring-teal-700"
              />
              New
            </label>
          </div>
          {playerMode === 'existing' ? (
            <label className="block">
              <span className="text-sm font-medium text-slate-700">Existing Player callsign</span>
              <select
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
                value={values.playerId ?? ''}
                onChange={(e) => setValues((current) => ({ ...current, playerId: e.target.value }))}
                required
              >
                <option value="">Select player callsign</option>
                {players.map((player) => (
                  <option key={player.id} value={player.id}>
                    {player.name}
                  </option>
                ))}
              </select>
            </label>
          ) : (
            <label className="block">
              <span className="text-sm font-medium text-slate-700">New Player callsign</span>
              <input
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
                maxLength={120}
                value={values.playerCallsign ?? ''}
                onChange={(e) => setValues((current) => ({ ...current, playerCallsign: e.target.value }))}
                required
              />
            </label>
          )}
        </fieldset>
      ) : (
        <label className="block">
          <span className="text-sm font-medium text-slate-700">Player callsign</span>
          <select
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
            value={values.playerId ?? ''}
            onChange={(e) => setValues((current) => ({ ...current, playerId: e.target.value }))}
            required
          >
            <option value="">Select player callsign</option>
            {players.map((player) => (
              <option key={player.id} value={player.id}>
                {player.name}
              </option>
            ))}
          </select>
        </label>
      )}

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
