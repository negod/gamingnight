import type { TotalTeamLeaderboardRow } from '../../../shared/types/leaderboard';

type Props = { rows: TotalTeamLeaderboardRow[] };

export function TotalTeamLeaderboard({ rows }: Props) {
  if (rows.length === 0) {
    return <p className="text-sm text-slate-500">No results yet.</p>;
  }

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-200 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
          <th className="pb-2 pr-4">#</th>
          <th className="pb-2 pr-4">Team</th>
          <th className="pb-2 text-right">Points</th>
        </tr>
      </thead>
      <tbody className="divide-y divide-slate-100">
        {rows.map((row) => (
          <tr key={row.teamId} className={row.rank === 1 ? 'font-semibold' : ''}>
            <td className="py-2 pr-4 font-mono text-slate-500">{row.rank}</td>
            <td className="py-2 pr-4 text-slate-900">{row.teamName}</td>
            <td className="py-2 text-right font-mono text-teal-700">{row.points}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
