import { getCurrentUser } from '../features/users/api/usersApi';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';

export function CurrentUserPage() {
  const { data, error, loading } = useAsync(getCurrentUser, []);

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">My user</h1>
        <p className="mt-1 text-sm text-slate-600">Your system role and linked player.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {data ? (
        <div className="rounded-lg border border-slate-200 bg-white p-5 text-sm">
          <dl className="grid gap-4 sm:grid-cols-2">
            <div>
              <dt className="font-medium text-slate-500">Username</dt>
              <dd className="mt-1 text-slate-950">{data.username}</dd>
            </div>
            <div>
              <dt className="font-medium text-slate-500">Email</dt>
              <dd className="mt-1 text-slate-950">{data.email || '-'}</dd>
            </div>
            <div>
              <dt className="font-medium text-slate-500">Role</dt>
              <dd className="mt-1 text-slate-950">{data.role === 'ADMIN' ? 'Admin' : 'User'}</dd>
            </div>
            <div>
              <dt className="font-medium text-slate-500">Player</dt>
              <dd className="mt-1 text-slate-950">{data.playerName}</dd>
            </div>
          </dl>
        </div>
      ) : null}
    </section>
  );
}
