import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { deleteTeam, listTeams } from '../features/teams/api/teamsApi';
import { TeamList } from '../features/teams/components/TeamList';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';

export function TeamsPage() {
  const [version, setVersion] = useState(0);
  const { data, error, loading } = useAsync(listTeams, [version]);

  async function handleDelete(id: string) {
    if (!confirm('Delete this team?')) return;
    await deleteTeam(id);
    setVersion((v) => v + 1);
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Teams</h1>
          <p className="mt-1 text-sm text-slate-600">Manage teams and their player assignments.</p>
        </div>
        <Link
          to="/teams/new"
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New team
        </Link>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {data ? <TeamList teams={data} onDelete={handleDelete} /> : null}
    </section>
  );
}
