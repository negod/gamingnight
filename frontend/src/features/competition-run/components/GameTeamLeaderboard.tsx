import type { GameTeamLeaderboard as GameTeamLeaderboardType } from '../../../shared/types/leaderboard';

type Props = { leaderboard: GameTeamLeaderboardType };

export function GameTeamLeaderboard({ leaderboard }: Props) {
  if (leaderboard.rows.length === 0) {
    return <p className="text-sm text-slate-500">No results yet.</p>;
  }

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-200 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
          <th className="pb-2 pr-4">#</th>
          <th className="pb-2 pr-4">Team</th>
          <th className="pb-2 text-right">{leaderboard.columnHeader}</th>
        </tr>
      </thead>
      <tbody className="divide-y divide-slate-100">
        {leaderboard.rows.map((row) => (
          <tr key={row.teamId}>
            <td className="py-2 pr-4 font-mono text-slate-500">{row.rank}</td>
            <td className="py-2 pr-4 font-medium text-slate-900">{row.teamName}</td>
            <td className="py-2 text-right font-mono text-slate-700">
              {row.value % 1 === 0 ? row.value.toFixed(0) : row.value.toFixed(2)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
