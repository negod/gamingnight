import { useNavigate } from 'react-router-dom';
import { createPlayer } from '../features/players/api/playersApi';
import { PlayerForm } from '../features/players/components/PlayerForm';
import type { PlayerFormValues } from '../shared/types/player';

export function CreatePlayerPage() {
  const navigate = useNavigate();

  async function handleSubmit(values: PlayerFormValues) {
    await createPlayer(values);
    navigate('/players');
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">New player</h1>
        <p className="mt-1 text-sm text-slate-600">Add a participant who can be assigned to teams.</p>
      </div>

      <PlayerForm submitLabel="Create player" onSubmit={handleSubmit} />
    </section>
  );
}
