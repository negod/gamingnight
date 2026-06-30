import { useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getGame, updateGame } from '../features/games/api/gamesApi';
import { GameForm } from '../features/games/components/GameForm';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { GameFormValues } from '../shared/types/game';

export function EditGamePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const loadGame = useCallback(() => getGame(id!), [id]);
  const { data: game, error, loading } = useAsync(loadGame, [id]);

  async function handleSubmit(values: GameFormValues) {
    await updateGame(id!, values);
    navigate('/games');
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Edit game</h1>
        <p className="mt-1 text-sm text-slate-600">Update the game name, type, or scoring rules.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {game ? (
        <GameForm
          initialValues={{
            name: game.name,
            description: game.description,
            platform: game.platform,
            genre: game.genre,
            isActive: game.isActive,
            matchType: game.matchType,
            participantRule: game.participantRule,
            resultType: game.resultType,
            winnerRule: game.winnerRule,
            scoringRule: game.scoringRule,
            tieBreakerRule: game.tieBreakerRule,
            validationRule: game.validationRule,
            rotationRule: game.rotationRule,
            timeLimitRule: game.timeLimitRule,
            bonusRules: game.bonusRules,
          }}
          submitLabel="Save changes"
          onSubmit={handleSubmit}
        />
      ) : null}
    </section>
  );
}
