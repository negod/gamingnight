import { useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getPlayer, updatePlayer } from '../features/players/api/playersApi';
import { PlayerForm } from '../features/players/components/PlayerForm';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import type { PlayerFormValues } from '../shared/types/player';

export function EditPlayerPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const loadPlayer = useCallback(() => getPlayer(id!), [id]);
  const { data: player, error, loading } = useAsync(loadPlayer, [id]);

  async function handleSubmit(values: PlayerFormValues) {
    await updatePlayer(id!, values);
    navigate('/players');
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Edit player</h1>
        <p className="mt-1 text-sm text-slate-600">Update the participant name.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {player ? (
        <PlayerForm
          initialValues={{ name: player.name }}
          submitLabel="Save changes"
          onSubmit={handleSubmit}
        />
      ) : null}
    </section>
  );
}
