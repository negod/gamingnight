import { Link } from 'react-router-dom';
import { Gamepad2, Pencil, Trash2 } from 'lucide-react';
import type { Game } from '../../../shared/types/game';

type GameListProps = {
  games: Game[];
  onDelete: (id: string) => void;
};

const GAME_TYPE_LABEL: Record<Game['gameType'], string> = {
  SCORE_BASED: 'Score',
  TIME_BASED: 'Time',
};

const CALC_METHOD_LABEL: Record<Game['calculationMethod'], string> = {
  SUM: 'Sum',
  AVERAGE: 'Average',
};

export function GameList({ games, onDelete }: GameListProps) {
  if (games.length === 0) {
    return <p className="text-sm text-slate-500">No games yet.</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-600">
          <tr>
            <th className="px-4 py-3 font-medium">Name</th>
            <th className="px-4 py-3 font-medium">Type</th>
            <th className="px-4 py-3 font-medium">Calculation</th>
            <th className="px-4 py-3 font-medium sr-only">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {games.map((game) => (
            <tr key={game.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-medium text-slate-900">
                <span className="flex items-center gap-2">
                  <Gamepad2 aria-hidden="true" className="h-4 w-4 text-slate-400" />
                  {game.name}
                </span>
              </td>
              <td className="px-4 py-3 text-slate-600">{GAME_TYPE_LABEL[game.gameType]}</td>
              <td className="px-4 py-3 text-slate-600">{CALC_METHOD_LABEL[game.calculationMethod]}</td>
              <td className="px-4 py-3">
                <div className="flex justify-end gap-2">
                  <Link
                    to={`/games/${game.id}/edit`}
                    className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                  >
                    <Pencil aria-hidden="true" className="h-3.5 w-3.5" />
                    Edit
                  </Link>
                  <button
                    onClick={() => onDelete(game.id)}
                    className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-red-600 hover:bg-red-50"
                  >
                    <Trash2 aria-hidden="true" className="h-3.5 w-3.5" />
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
