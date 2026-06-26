import { useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getTeam, listPlayers, updateTeam } from '../features/teams/api/teamsApi';
import { TeamForm } from '../features/teams/components/TeamForm';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { TeamFormValues } from '../shared/types/team';

export function EditTeamPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const loadTeam = useCallback(() => getTeam(id!), [id]);
  const loadPlayers = useCallback(() => listPlayers(), []);

  const { data: team, error: teamError, loading: teamLoading } = useAsync(loadTeam, [id]);
  const { data: players, error: playersError, loading: playersLoading } = useAsync(loadPlayers, []);

  async function handleSubmit(values: TeamFormValues) {
    await updateTeam(id!, values);
    navigate('/teams');
  }

  const loading = teamLoading || playersLoading;
  const error = teamError ?? playersError;

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Edit team</h1>
        <p className="mt-1 text-sm text-slate-600">Update the team name or player assignments.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {team && players ? (
        <TeamForm
          initialValues={{ name: team.name, playerIds: team.playerIds }}
          players={players}
          submitLabel="Save changes"
          onSubmit={handleSubmit}
        />
      ) : null}
    </section>
  );
}
