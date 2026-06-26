import { Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { listItems } from '../features/items/api/itemsApi';
import { ItemList } from '../features/items/components/ItemList';
import { useAsync } from '../features/items/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';

export function ItemsPage() {
  const { data, error, loading } = useAsync(listItems, []);

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Items</h1>
          <p className="mt-1 text-sm text-slate-600">Create, edit, and maintain items.</p>
        </div>
        <Link
          to="/items/new"
          className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          New item
        </Link>
      </div>

      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {data ? <ItemList items={data} /> : null}
    </section>
  );
}
