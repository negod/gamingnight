import type { GamePlayerLeaderboard as GamePlayerLeaderboardType } from '../../../shared/types/leaderboard';
import { formatResultValue } from './formatResultValue';

type Props = { leaderboard: GamePlayerLeaderboardType };

export function GamePlayerLeaderboard({ leaderboard }: Props) {
  if (leaderboard.rows.length === 0) {
    return <p className="text-sm text-slate-500">No results yet.</p>;
  }

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-200 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
          <th className="pb-2 pr-4">#</th>
          <th className="pb-2 pr-4">Player</th>
          <th className="pb-2 text-right">{leaderboard.columnHeader}</th>
        </tr>
      </thead>
      <tbody className="divide-y divide-slate-100">
        {leaderboard.rows.map((row) => (
          <tr key={row.playerId}>
            <td className="py-2 pr-4 font-mono text-slate-500">{row.rank}</td>
            <td className="py-2 pr-4 font-medium text-slate-900">{row.playerName}</td>
            <td className="py-2 text-right font-mono text-slate-700">
              {formatResultValue(row.value)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
