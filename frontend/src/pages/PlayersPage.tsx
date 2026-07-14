import { useState } from 'react';
import { ArrowLeft, Plus } from 'lucide-react';
import { createPlayer, deletePlayer, listPlayers, updatePlayer } from '../features/players/api/playersApi';
import { PlayerList } from '../features/players/components/PlayerList';
import { PlayerForm } from '../features/players/components/PlayerForm';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import type { Player, PlayerFormValues } from '../shared/types/player';

type Mode = 'list' | 'create' | 'edit';

export function PlayersPage() {
  const [version, setVersion] = useState(0);
  const [mode, setMode] = useState<Mode>('list');
  const [editingPlayer, setEditingPlayer] = useState<Player | null>(null);
  const { data, error, loading } = useAsync(listPlayers, [version]);

  function returnToList() {
    setMode('list');
    setEditingPlayer(null);
  }

  async function handleCreate(values: PlayerFormValues) {
    await createPlayer(values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleUpdate(values: PlayerFormValues) {
    if (!editingPlayer) return;
    await updatePlayer(editingPlayer.id, values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this player?')) return;
    await deletePlayer(id);
    setVersion((v) => v + 1);
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Players</h1>
          <p className="mt-1 text-sm text-slate-600">Register participants before assigning them to teams.</p>
        </div>
        {mode === 'list' ? (
        <button
          type="button"
          onClick={() => setMode('create')}
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New player
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
          Back to players
        </button>
      ) : null}
      {mode === 'create' ? <PlayerForm submitLabel="Create player" onSubmit={handleCreate} /> : null}
      {mode === 'edit' && editingPlayer ? (
        <PlayerForm initialValues={{ name: editingPlayer.name }} submitLabel="Save changes" onSubmit={handleUpdate} />
      ) : null}
      {mode === 'list' && data ? (
        <PlayerList
          players={data}
          onEdit={(player) => {
            setEditingPlayer(player);
            setMode('edit');
          }}
          onDelete={handleDelete}
        />
      ) : null}
    </section>
  );
}
