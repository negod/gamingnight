import { Pencil, Trash2, Users } from 'lucide-react';
import type { Team } from '../../../shared/types/team';

type TeamListProps = {
  teams: Team[];
  onEdit: (team: Team) => void;
  onDelete: (id: string) => void;
};

export function TeamList({ teams, onEdit, onDelete }: TeamListProps) {
  if (teams.length === 0) {
    return <p className="text-sm text-slate-500">No teams yet.</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-600">
          <tr>
            <th className="px-4 py-3 font-medium">Name</th>
            <th className="px-4 py-3 font-medium">Players</th>
            <th className="px-4 py-3 font-medium sr-only">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {teams.map((team) => (
            <tr key={team.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-medium text-slate-900">
                <button
                  type="button"
                  onClick={() => onEdit(team)}
                  className="inline-flex items-center gap-2 rounded-sm text-slate-900 hover:text-teal-700 focus:outline-none focus:ring-2 focus:ring-teal-100"
                >
                  <Users aria-hidden="true" className="h-4 w-4 text-slate-400" />
                  {team.name}
                </button>
              </td>
              <td className="px-4 py-3 text-slate-600">{team.playerIds.length}</td>
              <td className="px-4 py-3">
                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => onEdit(team)}
                    className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                  >
                    <Pencil aria-hidden="true" className="h-3.5 w-3.5" />
                    Edit
                  </button>
                  <button
                    onClick={() => onDelete(team.id)}
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
