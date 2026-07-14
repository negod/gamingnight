import { ExternalLink } from 'lucide-react';
import type { Game } from '../../../shared/types/game';
import { describeGameRules } from '../../games/components/gameRuleDescription';

type CompetitionGamesOverviewProps = {
  games: Game[];
};

function isSafeHttpUrl(url: string): boolean {
  return url.startsWith('http://') || url.startsWith('https://');
}

export function CompetitionGamesOverview({ games }: CompetitionGamesOverviewProps) {
  if (games.length === 0) return null;

  return (
    <div className="space-y-3">
      <h2 className="text-lg font-semibold text-slate-800">Games in this competition</h2>
      <div className="space-y-3">
        {games.map((game) => (
          <div key={game.id} className="rounded-lg border border-slate-200 bg-white p-4">
            <h3 className="font-semibold text-slate-900">{game.name}</h3>
            {game.description && (
              <p className="mt-1 text-sm leading-relaxed text-slate-700">{game.description}</p>
            )}
            <p className="mt-1 text-sm leading-relaxed text-slate-600">{describeGameRules(game)}</p>
            {game.referenceUrl && isSafeHttpUrl(game.referenceUrl) && (
              <a
                href={game.referenceUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-2 inline-flex items-center gap-1 text-sm font-medium text-teal-700 hover:text-teal-800 hover:underline"
              >
                More about this game
                <ExternalLink className="h-3.5 w-3.5" aria-hidden="true" />
              </a>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
