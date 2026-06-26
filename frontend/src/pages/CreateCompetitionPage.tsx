import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCompetition, listGames, listTeams } from '../features/competitions/api/competitionsApi';
import { CompetitionForm } from '../features/competitions/components/CompetitionForm';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { CompetitionFormValues } from '../shared/types/competition';

export function CreateCompetitionPage() {
  const navigate = useNavigate();
  const { data: games, error: gamesError, loading: gamesLoading } = useAsync(useCallback(listGames, []), []);
  const { data: teams, error: teamsError, loading: teamsLoading } = useAsync(useCallback(listTeams, []), []);

  async function handleSubmit(values: CompetitionFormValues) {
    await createCompetition(values);
    navigate('/competitions');
  }

  const loading = gamesLoading || teamsLoading;
  const error = gamesError ?? teamsError;

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">New competition</h1>
        <p className="mt-1 text-sm text-slate-600">Set up a new competition with games and teams.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {games && teams ? (
        <CompetitionForm games={games} teams={teams} submitLabel="Create competition" onSubmit={handleSubmit} />
      ) : null}
    </section>
  );
}
