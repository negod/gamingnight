import { useCallback, useEffect, useState } from 'react';
import { ArrowLeft, Play } from 'lucide-react';
import type { Competition } from '../shared/types/competition';
import type { Game } from '../shared/types/game';
import type { Match, PlayerResult } from '../shared/types/match';
import type { GameTeamLeaderboard, GamePlayerLeaderboard, TotalTeamLeaderboardRow, TotalPlayerLeaderboardRow } from '../shared/types/leaderboard';
import {
  startCompetition,
  getCompetition,
  getGame,
  getMatches,
  enterResults,
} from '../features/competition-run/api/competitionRunApi';
import {
  getGameTeamLeaderboard,
  getGamePlayerLeaderboard,
  getTotalTeamLeaderboard,
  getTotalPlayerLeaderboard,
} from '../features/competition-run/api/leaderboardApi';
import { CompetitionGamesOverview } from '../features/competition-run/components/CompetitionGamesOverview';
import { GameStepNav } from '../features/competition-run/components/GameStepNav';
import { MatchCard } from '../features/competition-run/components/MatchCard';
import { MatchResultForm } from '../features/competition-run/components/MatchResultForm';
import { useMatchDetails } from '../features/competition-run/hooks/useMatchDetails';
import { GameTeamLeaderboard as GameTeamLeaderboardView } from '../features/competition-run/components/GameTeamLeaderboard';
import { GamePlayerLeaderboard as GamePlayerLeaderboardView } from '../features/competition-run/components/GamePlayerLeaderboard';
import { TotalTeamLeaderboard } from '../features/competition-run/components/TotalTeamLeaderboard';
import { TotalPlayerLeaderboard } from '../features/competition-run/components/TotalPlayerLeaderboard';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAuth } from '../shared/auth/AuthContext';

type Tab = 'matches' | 'game-leaderboard' | 'total-leaderboard';

type CompetitionRunPageProps = {
  competitionId: string;
  onBack?: () => void;
};

export function CompetitionRunPage({ competitionId, onBack }: CompetitionRunPageProps) {
  const { user } = useAuth();
  const admin = user?.role === 'ADMIN';

  const [competition, setCompetition] = useState<Competition | null>(null);
  const [games, setGames] = useState<Game[]>([]);
  const [activeGameIndex, setActiveGameIndex] = useState(0);
  const [matches, setMatches] = useState<Match[]>([]);
  const [editingMatch, setEditingMatch] = useState<Match | null>(null);
  const { rows: editingRows, loading: editingRowsLoading, error: editingRowsError } = useMatchDetails(editingMatch);
  const [gameTeamLb, setGameTeamLb] = useState<GameTeamLeaderboard | null>(null);
  const [gamePlayerLb, setGamePlayerLb] = useState<GamePlayerLeaderboard | null>(null);
  const [totalTeamLb, setTotalTeamLb] = useState<TotalTeamLeaderboardRow[]>([]);
  const [totalPlayerLb, setTotalPlayerLb] = useState<TotalPlayerLeaderboardRow[]>([]);
  const [tab, setTab] = useState<Tab>('matches');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [starting, setStarting] = useState(false);

  useEffect(() => {
    setLoading(true);
    getCompetition(competitionId)
      .then(async (comp) => {
        setCompetition(comp);
        if (comp.gameIds.length > 0) {
          const loaded = await Promise.all(comp.gameIds.map(getGame));
          setGames(loaded);
        }
        setLoading(false);
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to load competition');
        setLoading(false);
      });
  }, [competitionId]);

  const activeGameId = games[activeGameIndex]?.id;

  const loadGameData = useCallback(
    async (gameId: string) => {
      const [m, teamLb, playerLb] = await Promise.all([
        getMatches(competitionId, gameId),
        getGameTeamLeaderboard(competitionId, gameId),
        getGamePlayerLeaderboard(competitionId, gameId),
      ]);
      setMatches(m);
      setGameTeamLb(teamLb);
      setGamePlayerLb(playerLb);
    },
    [competitionId],
  );

  const loadTotalLeaderboards = useCallback(async () => {
    const [teams, players] = await Promise.all([
      getTotalTeamLeaderboard(competitionId),
      getTotalPlayerLeaderboard(competitionId),
    ]);
    setTotalTeamLb(teams);
    setTotalPlayerLb(players);
  }, [competitionId]);

  useEffect(() => {
    if (!competition?.started || !activeGameId) return;
    loadGameData(activeGameId).catch(() => {});
    loadTotalLeaderboards().catch(() => {});
  }, [competition?.started, activeGameId, loadGameData, loadTotalLeaderboards]);

  async function handleStart() {
    setStarting(true);
    setError(null);
    try {
      const started = await startCompetition(competitionId);
      setCompetition(started);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start competition');
    } finally {
      setStarting(false);
    }
  }

  async function handleSaveResults(results: PlayerResult[]) {
    if (!editingMatch) return;
    const updated = await enterResults(competitionId, editingMatch.id, { results });
    setMatches((prev) => prev.map((m) => (m.id === updated.id ? updated : m)));
    setEditingMatch(null);
    if (activeGameId) {
      await loadGameData(activeGameId).catch(() => {});
      await loadTotalLeaderboards().catch(() => {});
    }
  }

  if (loading) return <LoadingMessage />;
  if (error) return <ErrorMessage message={error} />;
  if (!competition) return null;

  return (
    <section className="space-y-6">
      {onBack ? (
        <button
          type="button"
          onClick={onBack}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-950"
        >
          <ArrowLeft aria-hidden="true" className="h-4 w-4" />
          Back to competitions
        </button>
      ) : null}
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">{competition.name}</h1>
        <p className="mt-1 text-sm text-slate-600">{competition.date}</p>
      </div>

      <CompetitionGamesOverview games={games} />

      {!competition.started && admin && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-center">
          <p className="mb-4 text-slate-600">
            Ready to start? This will generate all match-ups and cannot be undone.
          </p>
          <button
            type="button"
            disabled={starting}
            onClick={handleStart}
            className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-5 py-2.5 text-sm font-semibold text-white hover:bg-teal-800 disabled:opacity-60"
          >
            <Play className="h-4 w-4" aria-hidden="true" />
            {starting ? 'Starting…' : 'Start competition'}
          </button>
        </div>
      )}

      {competition.started && games.length > 0 && (
        <>
          <div className="flex flex-wrap items-center justify-between gap-4">
            <GameStepNav
              games={games}
              activeIndex={activeGameIndex}
              onSelect={(i) => {
                setActiveGameIndex(i);
                setEditingMatch(null);
                setTab('matches');
              }}
            />
            <div className="flex gap-1 rounded-lg border border-slate-200 p-1">
              {(['matches', 'game-leaderboard', 'total-leaderboard'] as Tab[]).map((t) => (
                <button
                  key={t}
                  type="button"
                  onClick={() => setTab(t)}
                  className={[
                    'rounded px-3 py-1 text-sm font-medium transition-colors',
                    t === tab ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100',
                  ].join(' ')}
                >
                  {t === 'matches' ? 'Matches' : t === 'game-leaderboard' ? 'Game' : 'Overall'}
                </button>
              ))}
            </div>
          </div>

          {tab === 'matches' && (
            <div className="space-y-3">
              <h2 className="text-lg font-semibold text-slate-800">
                {games[activeGameIndex]?.name} — Matches
              </h2>
              {matches.length === 0 && (
                <p className="text-sm text-slate-500">No matches generated.</p>
              )}
              {matches.map((match) => {
                if (editingMatch?.id !== match.id) {
                  return (
                    <MatchCard key={match.id} match={match} game={games[activeGameIndex]} onEdit={admin ? setEditingMatch : undefined} />
                  );
                }
                if (editingRowsLoading) {
                  return <p key={match.id} className="text-sm text-slate-500">Loading players…</p>;
                }
                if (editingRowsError) {
                  return <ErrorMessage key={match.id} message={editingRowsError} />;
                }
                return (
                  <MatchResultForm
                    key={match.id}
                    match={match}
                    game={games[activeGameIndex]}
                    rows={editingRows}
                    onSave={handleSaveResults}
                    onCancel={() => setEditingMatch(null)}
                  />
                );
              })}
            </div>
          )}

          {tab === 'game-leaderboard' && (
            <div className="grid gap-6 md:grid-cols-2">
              <div className="rounded-lg border border-slate-200 bg-white p-4">
                <h2 className="mb-4 font-semibold text-slate-800">
                  {games[activeGameIndex]?.name} — Teams
                </h2>
                {gameTeamLb ? (
                  <GameTeamLeaderboardView leaderboard={gameTeamLb} />
                ) : (
                  <p className="text-sm text-slate-500">Loading…</p>
                )}
              </div>
              <div className="rounded-lg border border-slate-200 bg-white p-4">
                <h2 className="mb-4 font-semibold text-slate-800">
                  {games[activeGameIndex]?.name} — Players
                </h2>
                {gamePlayerLb ? (
                  <GamePlayerLeaderboardView leaderboard={gamePlayerLb} />
                ) : (
                  <p className="text-sm text-slate-500">Loading…</p>
                )}
              </div>
            </div>
          )}

          {tab === 'total-leaderboard' && (
            <div className="grid gap-6 md:grid-cols-2">
              <div className="rounded-lg border border-slate-200 bg-white p-4">
                <h2 className="mb-4 font-semibold text-slate-800">Overall — Teams</h2>
                <TotalTeamLeaderboard rows={totalTeamLb} />
              </div>
              <div className="rounded-lg border border-slate-200 bg-white p-4">
                <h2 className="mb-4 font-semibold text-slate-800">Overall — Players</h2>
                <TotalPlayerLeaderboard rows={totalPlayerLb} />
              </div>
            </div>
          )}
        </>
      )}
    </section>
  );
}
