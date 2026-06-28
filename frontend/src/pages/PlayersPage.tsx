import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { deletePlayer, listPlayers } from '../features/players/api/playersApi';
import { PlayerList } from '../features/players/components/PlayerList';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';

export function PlayersPage() {
  const [version, setVersion] = useState(0);
  const { data, error, loading } = useAsync(listPlayers, [version]);

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
        <Link
          to="/players/new"
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New player
        </Link>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {data ? <PlayerList players={data} onDelete={handleDelete} /> : null}
    </section>
  );
}
