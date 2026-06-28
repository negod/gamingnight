import { useNavigate } from 'react-router-dom';
import { createUser, listPlayers } from '../features/users/api/usersApi';
import { UserForm } from '../features/users/components/UserForm';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import type { UserFormValues } from '../shared/types/user';

export function CreateUserPage() {
  const navigate = useNavigate();
  const { data: players, error, loading } = useAsync(listPlayers, []);

  async function handleSubmit(values: UserFormValues) {
    await createUser(values);
    navigate('/users');
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">New user</h1>
        <p className="mt-1 text-sm text-slate-600">Create a system user and tie it to a player.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {players ? <UserForm players={players} submitLabel="Create user" onSubmit={handleSubmit} /> : null}
    </section>
  );
}
