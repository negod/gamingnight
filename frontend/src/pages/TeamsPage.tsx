import { useState } from 'react';
import { ArrowLeft, Plus } from 'lucide-react';
import { createTeam, deleteTeam, listPlayers, listTeams, updateTeam } from '../features/teams/api/teamsApi';
import { TeamList } from '../features/teams/components/TeamList';
import { TeamForm } from '../features/teams/components/TeamForm';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { Team, TeamFormValues } from '../shared/types/team';

type Mode = 'list' | 'create' | 'edit';

export function TeamsPage() {
  const [version, setVersion] = useState(0);
  const [mode, setMode] = useState<Mode>('list');
  const [editingTeam, setEditingTeam] = useState<Team | null>(null);
  const { data: teams, error: teamsError, loading: teamsLoading } = useAsync(listTeams, [version]);
  const { data: players, error: playersError, loading: playersLoading } = useAsync(listPlayers, []);

  function returnToList() {
    setMode('list');
    setEditingTeam(null);
  }

  async function handleCreate(values: TeamFormValues) {
    await createTeam(values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleUpdate(values: TeamFormValues) {
    if (!editingTeam) return;
    await updateTeam(editingTeam.id, values);
    returnToList();
    setVersion((v) => v + 1);
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this team?')) return;
    await deleteTeam(id);
    setVersion((v) => v + 1);
  }

  const loading = teamsLoading || playersLoading;
  const error = teamsError ?? playersError;

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Teams</h1>
          <p className="mt-1 text-sm text-slate-600">Manage teams and their player assignments.</p>
        </div>
        {mode === 'list' ? (
        <button
          type="button"
          onClick={() => setMode('create')}
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New team
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
          Back to teams
        </button>
      ) : null}
      {mode === 'create' && players ? (
        <TeamForm players={players} submitLabel="Create team" onSubmit={handleCreate} />
      ) : null}
      {mode === 'edit' && editingTeam && players ? (
        <TeamForm
          initialValues={{ name: editingTeam.name, playerIds: editingTeam.playerIds }}
          players={players}
          submitLabel="Save changes"
          onSubmit={handleUpdate}
        />
      ) : null}
      {mode === 'list' && teams ? (
        <TeamList
          teams={teams}
          onEdit={(team) => {
            setEditingTeam(team);
            setMode('edit');
          }}
          onDelete={handleDelete}
        />
      ) : null}
    </section>
  );
}
