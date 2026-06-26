import { ChevronLeft, ChevronRight } from 'lucide-react';
import type { Game } from '../../../shared/types/game';

type GameStepNavProps = {
  games: Game[];
  activeIndex: number;
  onSelect: (index: number) => void;
};

export function GameStepNav({ games, activeIndex, onSelect }: GameStepNavProps) {
  return (
    <div className="flex items-center gap-2">
      <button
        type="button"
        disabled={activeIndex === 0}
        onClick={() => onSelect(activeIndex - 1)}
        className="rounded-md border border-slate-300 p-1.5 text-slate-600 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
        aria-label="Previous game"
      >
        <ChevronLeft className="h-4 w-4" aria-hidden="true" />
      </button>

      <ol className="flex gap-1">
        {games.map((game, index) => (
          <li key={game.id}>
            <button
              type="button"
              onClick={() => onSelect(index)}
              className={[
                'rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
                index === activeIndex
                  ? 'bg-teal-700 text-white'
                  : 'border border-slate-300 text-slate-700 hover:bg-slate-100',
              ].join(' ')}
            >
              {game.name}
            </button>
          </li>
        ))}
      </ol>

      <button
        type="button"
        disabled={activeIndex === games.length - 1}
        onClick={() => onSelect(activeIndex + 1)}
        className="rounded-md border border-slate-300 p-1.5 text-slate-600 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
        aria-label="Next game"
      >
        <ChevronRight className="h-4 w-4" aria-hidden="true" />
      </button>
    </div>
  );
}
