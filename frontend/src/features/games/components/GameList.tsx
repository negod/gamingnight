import { Gamepad2, Pencil, Trash2 } from 'lucide-react';
import type { Game, MatchType, ResultType, WinnerRule } from '../../../shared/types/game';

type GameListProps = {
  games: Game[];
  onEdit: (game: Game) => void;
  onDelete: (id: string) => void;
};

const MATCH_TYPE_LABEL: Record<MatchType, string> = {
  PLAYER_VS_PLAYER: '1v1',
  TEAM_VS_TEAM: 'Team vs Team',
  FREE_FOR_ALL: 'Free for All',
  SOLO_CHALLENGE: 'Solo',
  COOP_VS_AI: 'Co-op vs AI',
};

const RESULT_TYPE_LABEL: Record<ResultType, string> = {
  SCORE: 'Score',
  TIME: 'Time',
  PLACEMENT: 'Placement',
  WINNER_ONLY: 'Winner Only',
  KILLS: 'Kills',
  GOALS: 'Goals',
  ROUNDS_WON: 'Rounds Won',
  CUSTOM_NUMBER: 'Custom',
};

const WINNER_RULE_LABEL: Record<WinnerRule, string> = {
  HIGHEST_VALUE_WINS: 'Highest wins',
  LOWEST_VALUE_WINS: 'Lowest wins',
  FIRST_TO_FINISH_WINS: 'First to finish',
  LAST_REMAINING_WINS: 'Last remaining',
  MOST_ROUNDS_WON: 'Most rounds',
  MANUAL_WINNER: 'Manual',
  CLOSEST_TO_TARGET: 'Closest to target',
};

export function GameList({ games, onEdit, onDelete }: GameListProps) {
  if (games.length === 0) {
    return <p className="text-sm text-slate-500">No games yet.</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-600">
          <tr>
            <th className="px-4 py-3 font-medium">Name</th>
            <th className="px-4 py-3 font-medium">Match type</th>
            <th className="px-4 py-3 font-medium">Result</th>
            <th className="px-4 py-3 font-medium">Winner rule</th>
            <th className="px-4 py-3 font-medium">Status</th>
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
              <td className="px-4 py-3 text-slate-600">{MATCH_TYPE_LABEL[game.matchType]}</td>
              <td className="px-4 py-3 text-slate-600">{RESULT_TYPE_LABEL[game.resultType]}</td>
              <td className="px-4 py-3 text-slate-600">{WINNER_RULE_LABEL[game.winnerRule]}</td>
              <td className="px-4 py-3">
                {game.isActive ? (
                  <span className="inline-flex items-center rounded-full bg-teal-50 px-2 py-0.5 text-xs font-medium text-teal-700">
                    Active
                  </span>
                ) : (
                  <span className="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-500">
                    Inactive
                  </span>
                )}
              </td>
              <td className="px-4 py-3">
                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => onEdit(game)}
                    className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                  >
                    <Pencil aria-hidden="true" className="h-3.5 w-3.5" />
                    Edit
                  </button>
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
