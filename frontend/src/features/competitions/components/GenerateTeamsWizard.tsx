import { Shuffle } from 'lucide-react';
import { useState } from 'react';
import type { Player } from '../../../shared/types/player';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';

type GenerateTeamsWizardProps = {
  players: Player[];
  onGenerate: (playerIds: string[], teamSize: number) => Promise<void>;
};

export function GenerateTeamsWizard({ players, onGenerate }: GenerateTeamsWizardProps) {
  const [selectedPlayerIds, setSelectedPlayerIds] = useState<string[]>([]);
  const [teamSize, setTeamSize] = useState(2);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function togglePlayer(id: string) {
    setSelectedPlayerIds((prev) =>
      prev.includes(id) ? prev.filter((p) => p !== id) : [...prev, id],
    );
  }

  function selectAll() {
    setSelectedPlayerIds(players.map((p) => p.id));
  }

  function clearAll() {
    setSelectedPlayerIds([]);
  }

  const numTeams = teamSize > 0 ? Math.max(1, Math.floor(selectedPlayerIds.length / teamSize)) : 0;
  const leftover = teamSize > 0 ? selectedPlayerIds.length % teamSize : 0;

  async function handleGenerate() {
    setError(null);
    if (selectedPlayerIds.length === 0) {
      setError('Select at least one player');
      return;
    }
    setGenerating(true);
    try {
      await onGenerate(selectedPlayerIds, teamSize);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate teams');
    } finally {
      setGenerating(false);
    }
  }

  return (
    <div className="rounded-lg border border-teal-200 bg-teal-50 p-4 space-y-4">
      <div className="flex items-center gap-2">
        <Shuffle aria-hidden="true" className="h-4 w-4 text-teal-700" />
        <span className="text-sm font-semibold text-teal-800">Auto-generate teams</span>
      </div>

      {error ? <ErrorMessage message={error} /> : null}

      <fieldset>
        <div className="flex items-center justify-between">
          <legend className="text-sm font-medium text-slate-700">Select players</legend>
          <div className="flex gap-2">
            <button type="button" onClick={selectAll} className="text-xs text-teal-700 hover:underline">All</button>
            <button type="button" onClick={clearAll} className="text-xs text-slate-500 hover:underline">None</button>
          </div>
        </div>
        {players.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500">No players available.</p>
        ) : (
          <div className="mt-2 grid gap-2 sm:grid-cols-2">
            {players.map((player) => (
              <label key={player.id} className="flex cursor-pointer items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 hover:bg-slate-50">
                <input
                  type="checkbox"
                  checked={selectedPlayerIds.includes(player.id)}
                  onChange={() => togglePlayer(player.id)}
                  className="h-4 w-4 accent-teal-700"
                />
                <span className="text-sm text-slate-800">{player.name}</span>
              </label>
            ))}
          </div>
        )}
      </fieldset>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Players per team</span>
        <input
          type="number"
          min={1}
          value={teamSize}
          onChange={(e) => setTeamSize(Math.max(1, parseInt(e.target.value, 10) || 1))}
          className="mt-1 w-28 rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
        />
      </label>

      {selectedPlayerIds.length > 0 && (
        <p className="text-xs text-slate-600">
          Will create <strong>{numTeams}</strong> team{numTeams !== 1 ? 's' : ''}
          {leftover > 0 ? ` — ${leftover} player${leftover > 1 ? 's' : ''} distributed one per team` : ''}.
        </p>
      )}

      <button
        type="button"
        onClick={handleGenerate}
        disabled={generating || selectedPlayerIds.length === 0}
        className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <Shuffle aria-hidden="true" className="h-4 w-4" />
        {generating ? 'Generating...' : 'Generate teams'}
      </button>
    </div>
  );
}
