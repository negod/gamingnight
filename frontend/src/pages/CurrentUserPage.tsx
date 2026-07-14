import { FormEvent, useEffect, useState } from 'react';
import { Save } from 'lucide-react';
import { getCurrentUser, updateCurrentUser } from '../features/users/api/usersApi';
import type { AppUser, CurrentUserFormValues } from '../shared/types/user';
import { useAuth } from '../shared/auth/AuthContext';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';

export function CurrentUserPage() {
  const { data, error, loading } = useAsync(getCurrentUser, []);
  const auth = useAuth();
  const [currentUser, setCurrentUser] = useState<AppUser | null>(null);

  useEffect(() => {
    if (data) setCurrentUser(data);
  }, [data]);

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">My user</h1>
        <p className="mt-1 text-sm text-slate-600">Your username, email, and Player callsign.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {currentUser ? (
        <CurrentUserForm
          user={currentUser}
          onSubmit={async (values) => {
            const updated = await updateCurrentUser(values);
            setCurrentUser(updated);
            auth.updateUser(updated);
          }}
        />
      ) : null}
    </section>
  );
}

type CurrentUserFormProps = {
  user: AppUser;
  onSubmit: (values: CurrentUserFormValues) => Promise<void>;
};

function CurrentUserForm({ user, onSubmit }: CurrentUserFormProps) {
  const [values, setValues] = useState<CurrentUserFormValues>({
    email: user.email ?? '',
    playerCallsign: user.playerName,
  });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!values.playerCallsign.trim()) {
      setError('Player callsign is required');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit({
        email: values.email?.trim() ?? '',
        playerCallsign: values.playerCallsign.trim(),
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save user');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6 rounded-lg border border-slate-200 bg-white p-5 text-sm">
      {error ? <ErrorMessage message={error} /> : null}

      <div>
        <span className="text-sm font-medium text-slate-700">Username</span>
        <p className="mt-1 text-slate-950">{user.username}</p>
      </div>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Email</span>
        <input
          type="email"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={320}
          value={values.email ?? ''}
          onChange={(event) => setValues((current) => ({ ...current, email: event.target.value }))}
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Player callsign</span>
        <input
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={120}
          value={values.playerCallsign}
          onChange={(event) => setValues((current) => ({ ...current, playerCallsign: event.target.value }))}
          required
        />
      </label>

      <button
        type="submit"
        disabled={submitting}
        className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <Save aria-hidden="true" className="h-4 w-4" />
        {submitting ? 'Saving...' : 'Save changes'}
      </button>
    </form>
  );
}
