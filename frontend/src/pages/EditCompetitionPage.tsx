import { useCallback, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  generateTeams,
  getCompetition,
  listGames,
  listTeams,
  updateCompetition,
} from '../features/competitions/api/competitionsApi';
import { listPlayers } from '../features/teams/api/teamsApi';
import { CompetitionForm } from '../features/competitions/components/CompetitionForm';
import { GenerateTeamsWizard } from '../features/competitions/components/GenerateTeamsWizard';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { CompetitionFormValues } from '../shared/types/competition';

export function EditCompetitionPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [version, setVersion] = useState(0);

  const loadCompetition = useCallback(() => getCompetition(id!), [id, version]);
  const { data: competition, error: compError, loading: compLoading } = useAsync(loadCompetition, [id, version]);
  const { data: games, error: gamesError, loading: gamesLoading } = useAsync(useCallback(listGames, []), []);
  const { data: teams, error: teamsError, loading: teamsLoading } = useAsync(useCallback(listTeams, []), [version]);
  const { data: players, error: playersError, loading: playersLoading } = useAsync(useCallback(listPlayers, []), []);

  async function handleSubmit(values: CompetitionFormValues) {
    await updateCompetition(id!, values);
    navigate('/competitions');
  }

  async function handleGenerateTeams(playerIds: string[], teamSize: number) {
    await generateTeams(id!, playerIds, teamSize);
    setVersion((v) => v + 1);
  }

  const loading = compLoading || gamesLoading || teamsLoading || playersLoading;
  const error = compError ?? gamesError ?? teamsError ?? playersError;

  if (competition?.started) {
    return (
      <section className="space-y-4">
        <h1 className="text-2xl font-semibold text-slate-950">{competition.name}</h1>
        <p className="text-sm text-amber-700 rounded-md border border-amber-200 bg-amber-50 px-4 py-3">
          This competition has started and can no longer be edited.
        </p>
      </section>
    );
  }

  return (
    <section className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Edit competition</h1>
        <p className="mt-1 text-sm text-slate-600">Update competition details, games, and teams.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}

      {competition && games && teams ? (
        <CompetitionForm
          initialValues={{
            name: competition.name,
            date: competition.date,
            singleMatch: competition.singleMatch,
            registrationOpen: competition.registrationOpen,
            gameIds: competition.gameIds,
            teamIds: competition.teamIds,
          }}
          games={games}
          teams={teams}
          submitLabel="Save changes"
          onSubmit={handleSubmit}
        />
      ) : null}

      {competition && players ? (
        <div className="space-y-2">
          <h2 className="text-base font-semibold text-slate-800">Auto-generate teams</h2>
          <p className="text-sm text-slate-500">
            Randomly shuffle players into new teams and assign them to this competition.
            Existing team assignments will be replaced.
          </p>
          <GenerateTeamsWizard
            players={players}
            initialSelectedPlayerIds={competition.registeredPlayerIds}
            onGenerate={handleGenerateTeams}
          />
        </div>
      ) : null}
    </section>
  );
}
