import { Pencil } from 'lucide-react';
import type { Match } from '../../../shared/types/match';

type MatchCardProps = {
  match: Match;
  onEdit: (match: Match) => void;
};

export function MatchCard({ match, onEdit }: MatchCardProps) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="flex items-center justify-between gap-4">
        <div className="flex flex-1 items-center justify-between gap-2">
          <span className="font-medium text-slate-900">{match.homeTeamName}</span>
          <span className="text-sm text-slate-400">vs</span>
          <span className="font-medium text-slate-900">{match.awayTeamName}</span>
        </div>

        <div className="flex items-center gap-2">
          {match.completed ? (
            <span className="rounded-full bg-teal-100 px-2 py-0.5 text-xs font-medium text-teal-800">
              Done
            </span>
          ) : (
            <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800">
              Pending
            </span>
          )}
          <button
            type="button"
            onClick={() => onEdit(match)}
            className="rounded-md border border-slate-300 p-1.5 text-slate-600 hover:bg-slate-100"
            aria-label={`Enter results for ${match.homeTeamName} vs ${match.awayTeamName}`}
          >
            <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
          </button>
        </div>
      </div>

      {match.completed && match.results.length > 0 && (
        <ul className="mt-3 space-y-1 border-t border-slate-100 pt-3">
          {match.results.map((r, i) => (
            <li key={i} className="flex justify-between text-sm text-slate-600">
              <span>{r.teamId}</span>
              <span className="font-mono">{r.value}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
