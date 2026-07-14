import { useState } from 'react';
import { ArrowLeft, Plus } from 'lucide-react';
import { createGame, deleteGame, listGames, updateGame } from '../features/games/api/gamesApi';
import { GameList } from '../features/games/components/GameList';
import { GameForm } from '../features/games/components/GameForm';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { Game, GameFormValues } from '../shared/types/game';

type Mode = 'list' | 'create' | 'edit';

export function GamesPage() {
  const [version, setVersion] = useState(0);
  const [mode, setMode] = useState<Mode>('list');
  const [editingGame, setEditingGame] = useState<Game | null>(null);
  const { data, error, loading } = useAsync(listGames, [version]);

  function returnToList() {
    setMode('list');
    setEditingGame(null);
  }

  async function handleCreate(values: GameFormValues) {
    await createGame(values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleUpdate(values: GameFormValues) {
    if (!editingGame) return;
    await updateGame(editingGame.id, values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this game?')) return;
    await deleteGame(id);
    setVersion((v) => v + 1);
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Games</h1>
          <p className="mt-1 text-sm text-slate-600">Manage games and their scoring rules.</p>
        </div>
        {mode === 'list' ? (
        <button
          type="button"
          onClick={() => setMode('create')}
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New game
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
          Back to games
        </button>
      ) : null}
      {mode === 'create' ? <GameForm submitLabel="Create game" onSubmit={handleCreate} /> : null}
      {mode === 'edit' && editingGame ? (
        <GameForm
          initialValues={{
            name: editingGame.name,
            description: editingGame.description,
            platform: editingGame.platform,
            genre: editingGame.genre,
            referenceUrl: editingGame.referenceUrl,
            isActive: editingGame.isActive,
            matchType: editingGame.matchType,
            participantRule: editingGame.participantRule,
            resultType: editingGame.resultType,
            winnerRule: editingGame.winnerRule,
            scoringRule: editingGame.scoringRule,
            tieBreakerRule: editingGame.tieBreakerRule,
            validationRule: editingGame.validationRule,
            rotationRule: editingGame.rotationRule,
            timeLimitRule: editingGame.timeLimitRule,
            bonusRules: editingGame.bonusRules,
          }}
          submitLabel="Save changes"
          onSubmit={handleUpdate}
        />
      ) : null}
      {mode === 'list' && data ? (
        <GameList
          games={data}
          onEdit={(game) => {
            setEditingGame(game);
            setMode('edit');
          }}
          onDelete={handleDelete}
        />
      ) : null}
    </section>
  );
}
