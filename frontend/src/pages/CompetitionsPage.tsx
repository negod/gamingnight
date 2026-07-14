import { useCallback, useState } from 'react';
import { ArrowLeft, Plus } from 'lucide-react';
import {
  createCompetition,
  deleteCompetition,
  generateTeams,
  listCompetitions,
  listGames,
  listTeams,
  registerForCompetition,
  unregisterFromCompetition,
  updateCompetition,
} from '../features/competitions/api/competitionsApi';
import { listPlayers } from '../features/teams/api/teamsApi';
import { CompetitionList } from '../features/competitions/components/CompetitionList';
import { CompetitionForm } from '../features/competitions/components/CompetitionForm';
import { GenerateTeamsWizard } from '../features/competitions/components/GenerateTeamsWizard';
import { CompetitionRunPage } from './CompetitionRunPage';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAuth } from '../shared/auth/AuthContext';
import type { Competition, CompetitionFormValues } from '../shared/types/competition';

type Mode = 'list' | 'create' | 'edit' | 'run';

export function CompetitionsPage() {
  const { user } = useAuth();
  const admin = user?.role === 'ADMIN';
  const [version, setVersion] = useState(0);
  const [mode, setMode] = useState<Mode>('list');
  const [selectedCompetition, setSelectedCompetition] = useState<Competition | null>(null);
  const { data: competitions, error: competitionsError, loading: competitionsLoading } = useAsync(listCompetitions, [version]);
  const { data: games, error: gamesError, loading: gamesLoading } = useAsync(useCallback(listGames, []), []);
  const { data: teams, error: teamsError, loading: teamsLoading } = useAsync(useCallback(listTeams, []), [version]);
  const { data: players, error: playersError, loading: playersLoading } = useAsync(useCallback(listPlayers, []), []);

  function returnToList() {
    setMode('list');
    setSelectedCompetition(null);
  }

  async function handleCreate(values: CompetitionFormValues) {
    await createCompetition(values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleUpdate(values: CompetitionFormValues) {
    if (!selectedCompetition) return;
    await updateCompetition(selectedCompetition.id, values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleGenerateTeams(playerIds: string[], teamSize: number) {
    if (!selectedCompetition) return;
    const updated = await generateTeams(selectedCompetition.id, playerIds, teamSize);
    setSelectedCompetition(updated);
    setVersion((v) => v + 1);
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this competition?')) return;
    await deleteCompetition(id);
    setVersion((v) => v + 1);
  }

  async function handleRegister(id: string) {
    await registerForCompetition(id);
    setVersion((v) => v + 1);
  }

  async function handleUnregister(id: string) {
    await unregisterFromCompetition(id);
    setVersion((v) => v + 1);
  }

  const loading = competitionsLoading || gamesLoading || teamsLoading || playersLoading;
  const error = competitionsError ?? gamesError ?? teamsError ?? playersError;

  if (mode === 'run' && selectedCompetition) {
    return <CompetitionRunPage competitionId={selectedCompetition.id} onBack={returnToList} />;
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Competitions</h1>
          <p className="mt-1 text-sm text-slate-600">Create and manage competitions.</p>
        </div>
        {admin && mode === 'list' ? (
          <button
            type="button"
            onClick={() => setMode('create')}
            className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
          >
            <Plus aria-hidden="true" className="h-4 w-4" />
            New competition
          </button>
        ) : null}
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {mode !== 'list' ? (
        <button
          type="button"
          onClick={returnToList}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-950"
        >
          <ArrowLeft aria-hidden="true" className="h-4 w-4" />
          Back to competitions
        </button>
      ) : null}

      {mode === 'create' && games && teams ? (
        <CompetitionForm games={games} teams={teams} submitLabel="Create competition" onSubmit={handleCreate} />
      ) : null}

      {mode === 'edit' && selectedCompetition?.started ? (
        <p className="text-sm text-amber-700 rounded-md border border-amber-200 bg-amber-50 px-4 py-3">
          This competition has started and can no longer be edited.
        </p>
      ) : null}

      {mode === 'edit' && selectedCompetition && !selectedCompetition.started && games && teams ? (
        <div className="space-y-8">
          <CompetitionForm
            initialValues={{
              name: selectedCompetition.name,
              date: selectedCompetition.date,
              singleMatch: selectedCompetition.singleMatch,
              registrationOpen: selectedCompetition.registrationOpen,
              gameIds: selectedCompetition.gameIds,
              teamIds: selectedCompetition.teamIds,
            }}
            games={games}
            teams={teams}
            submitLabel="Save changes"
            onSubmit={handleUpdate}
          />
          {players ? (
            <div className="space-y-2">
              <h2 className="text-base font-semibold text-slate-800">Auto-generate teams</h2>
              <p className="text-sm text-slate-500">
                Randomly shuffle players into new teams and assign them to this competition.
                Existing team assignments will be replaced.
              </p>
              <GenerateTeamsWizard
                players={players}
                initialSelectedPlayerIds={selectedCompetition.registeredPlayerIds}
                onGenerate={handleGenerateTeams}
              />
            </div>
          ) : null}
        </div>
      ) : null}

      {mode === 'list' && competitions ? (
        <CompetitionList
          competitions={competitions}
          onEdit={(competition) => {
            setSelectedCompetition(competition);
            setMode('edit');
          }}
          onRun={(competition) => {
            setSelectedCompetition(competition);
            setMode('run');
          }}
          onDelete={handleDelete}
          onRegister={handleRegister}
          onUnregister={handleUnregister}
          canManage={admin}
          currentPlayerId={user?.playerId}
        />
      ) : null}
    </section>
  );
}
