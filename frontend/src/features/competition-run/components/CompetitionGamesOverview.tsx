import type { Game } from '../../../shared/types/game';
import { describeGameRules } from '../../games/components/gameRuleDescription';

type CompetitionGamesOverviewProps = {
  games: Game[];
};

export function CompetitionGamesOverview({ games }: CompetitionGamesOverviewProps) {
  if (games.length === 0) return null;

  return (
    <div className="space-y-3">
      <h2 className="text-lg font-semibold text-slate-800">Games in this competition</h2>
      <div className="space-y-3">
        {games.map((game) => (
          <div key={game.id} className="rounded-lg border border-slate-200 bg-white p-4">
            <h3 className="font-semibold text-slate-900">{game.name}</h3>
            <p className="mt-1 text-sm leading-relaxed text-slate-600">{describeGameRules(game)}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
