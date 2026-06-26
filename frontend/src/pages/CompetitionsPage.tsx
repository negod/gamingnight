import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { deleteCompetition, listCompetitions } from '../features/competitions/api/competitionsApi';
import { CompetitionList } from '../features/competitions/components/CompetitionList';
import { useAsync } from '../shared/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';

export function CompetitionsPage() {
  const [version, setVersion] = useState(0);
  const { data, error, loading } = useAsync(listCompetitions, [version]);

  async function handleDelete(id: string) {
    if (!confirm('Delete this competition?')) return;
    await deleteCompetition(id);
    setVersion((v) => v + 1);
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Competitions</h1>
          <p className="mt-1 text-sm text-slate-600">Create and manage competitions.</p>
        </div>
        <Link
          to="/competitions/new"
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New competition
        </Link>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {data ? <CompetitionList competitions={data} onDelete={handleDelete} /> : null}
    </section>
  );
}
