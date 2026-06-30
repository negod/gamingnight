import { useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getUser, listPlayers, updateUser } from '../features/users/api/usersApi';
import { UserForm } from '../features/users/components/UserForm';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import type { UserFormValues } from '../shared/types/user';

export function EditUserPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const loadData = useCallback(async () => {
    const [user, players] = await Promise.all([getUser(id!), listPlayers()]);
    return { user, players };
  }, [id]);
  const { data, error, loading } = useAsync(loadData, [id]);

  async function handleSubmit(values: UserFormValues) {
    await updateUser(id!, values);
    navigate('/users');
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Edit user</h1>
        <p className="mt-1 text-sm text-slate-600">Update role or player assignment.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {data ? (
        <UserForm
          players={data.players}
          initialValues={{
            username: data.user.username,
            email: data.user.email || '',
            role: data.user.role,
            playerId: data.user.playerId,
          }}
          submitLabel="Save changes"
          onSubmit={handleSubmit}
        />
      ) : null}
    </section>
  );
}
