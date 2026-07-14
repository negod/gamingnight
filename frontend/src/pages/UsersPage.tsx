import { useState } from 'react';
import { ArrowLeft, Plus } from 'lucide-react';
import { createUser, deleteUser, listPlayers, listUsers, updateUser } from '../features/users/api/usersApi';
import { UserList } from '../features/users/components/UserList';
import { UserForm } from '../features/users/components/UserForm';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import type { AppUser, UserFormValues } from '../shared/types/user';

type Mode = 'list' | 'create' | 'edit';

export function UsersPage() {
  const [version, setVersion] = useState(0);
  const [playersVersion, setPlayersVersion] = useState(0);
  const [mode, setMode] = useState<Mode>('list');
  const [editingUser, setEditingUser] = useState<AppUser | null>(null);
  const { data: users, error: usersError, loading: usersLoading } = useAsync(listUsers, [version]);
  const { data: players, error: playersError, loading: playersLoading } = useAsync(listPlayers, [playersVersion]);

  function returnToList() {
    setMode('list');
    setEditingUser(null);
  }

  async function handleCreate(values: UserFormValues) {
    await createUser(values);
    returnToList();
    setVersion((v) => v + 1);
    setPlayersVersion((v) => v + 1);
  }

  async function handleUpdate(values: UserFormValues) {
    if (!editingUser) return;
    await updateUser(editingUser.id, values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this user?')) return;
    await deleteUser(id);
    setVersion((v) => v + 1);
  }

  const loading = usersLoading || playersLoading;
  const error = usersError ?? playersError;

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Users</h1>
          <p className="mt-1 text-sm text-slate-600">Administer system users, roles, and Player callsign links.</p>
        </div>
        {mode === 'list' ? (
        <button
          type="button"
          onClick={() => setMode('create')}
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New user
        </button>
        ) : null}
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {mode !== 'list' ? (
        <button
          type="button"
          onClick={returnToList}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-950"
        >
          <ArrowLeft aria-hidden="true" className="h-4 w-4" />
          Back to users
        </button>
      ) : null}
      {mode === 'create' && players ? (
        <UserForm players={players} submitLabel="Create user" onSubmit={handleCreate} />
      ) : null}
      {mode === 'edit' && editingUser && players ? (
        <UserForm
          players={players}
          initialValues={{
            username: editingUser.username,
            email: editingUser.email || '',
            role: editingUser.role,
            playerId: editingUser.playerId,
          }}
          submitLabel="Save changes"
          onSubmit={handleUpdate}
        />
      ) : null}
      {mode === 'list' && users ? (
        <UserList
          users={users}
          onEdit={(user) => {
            setEditingUser(user);
            setMode('edit');
          }}
          onDelete={handleDelete}
        />
      ) : null}
    </section>
  );
}
