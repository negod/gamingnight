import { Link } from 'react-router-dom';
import { Pencil, Play, Trash2, Trophy } from 'lucide-react';
import type { Competition } from '../../../shared/types/competition';

type CompetitionListProps = {
  competitions: Competition[];
  onDelete: (id: string) => void;
  canManage?: boolean;
};

export function CompetitionList({ competitions, onDelete, canManage = true }: CompetitionListProps) {
  if (competitions.length === 0) {
    return <p className="text-sm text-slate-500">No competitions yet.</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-600">
          <tr>
            <th className="px-4 py-3 font-medium">Name</th>
            <th className="px-4 py-3 font-medium">Date</th>
            <th className="px-4 py-3 font-medium">Teams</th>
            <th className="px-4 py-3 font-medium">Games</th>
            <th className="px-4 py-3 font-medium">Status</th>
            <th className="px-4 py-3 font-medium sr-only">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {competitions.map((competition) => (
            <tr key={competition.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-medium text-slate-900">
                <span className="flex items-center gap-2">
                  <Trophy aria-hidden="true" className="h-4 w-4 text-slate-400" />
                  {competition.name}
                </span>
              </td>
              <td className="px-4 py-3 text-slate-600">{competition.date}</td>
              <td className="px-4 py-3 text-slate-600">{competition.teamIds.length}</td>
              <td className="px-4 py-3 text-slate-600">{competition.gameIds.length}</td>
              <td className="px-4 py-3">
                {competition.started ? (
                  <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-700">
                    Started
                  </span>
                ) : (
                  <span className="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600">
                    Setup
                  </span>
                )}
              </td>
              <td className="px-4 py-3">
                <div className="flex justify-end gap-2">
                  {canManage && !competition.started && (
                    <Link
                      to={`/competitions/${competition.id}/edit`}
                      className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                    >
                      <Pencil aria-hidden="true" className="h-3.5 w-3.5" />
                      Edit
                    </Link>
                  )}
                  <Link
                    to={`/competitions/${competition.id}/run`}
                    className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-teal-700 hover:bg-teal-50"
                  >
                    <Play aria-hidden="true" className="h-3.5 w-3.5" />
                    {competition.started || !canManage ? 'View' : 'Run'}
                  </Link>
                  {canManage ? (
                    <button
                      onClick={() => onDelete(competition.id)}
                      className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-red-600 hover:bg-red-50"
                    >
                      <Trash2 aria-hidden="true" className="h-3.5 w-3.5" />
                      Delete
                    </button>
                  ) : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
