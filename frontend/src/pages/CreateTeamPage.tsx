import { useNavigate } from 'react-router-dom';
import { createTeam, listPlayers } from '../features/teams/api/teamsApi';
import { TeamForm } from '../features/teams/components/TeamForm';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { TeamFormValues } from '../shared/types/team';

export function CreateTeamPage() {
  const navigate = useNavigate();
  const { data: players, error, loading } = useAsync(listPlayers, []);

  async function handleSubmit(values: TeamFormValues) {
    await createTeam(values);
    navigate('/teams');
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">New team</h1>
        <p className="mt-1 text-sm text-slate-600">Create a team and assign players to it.</p>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {players ? (
        <TeamForm players={players} submitLabel="Create team" onSubmit={handleSubmit} />
      ) : null}
    </section>
  );
}
