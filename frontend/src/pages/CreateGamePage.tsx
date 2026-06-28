import { useNavigate } from 'react-router-dom';
import { createGame } from '../features/games/api/gamesApi';
import { GameForm } from '../features/games/components/GameForm';
import type { GameFormValues } from '../shared/types/game';

export function CreateGamePage() {
  const navigate = useNavigate();

  async function handleSubmit(values: GameFormValues) {
    await createGame(values);
    navigate('/games');
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">New game</h1>
        <p className="mt-1 text-sm text-slate-600">Define a game and its scoring rules.</p>
      </div>
      <GameForm submitLabel="Create game" onSubmit={handleSubmit} />
    </section>
  );
}
