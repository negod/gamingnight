import { Link, useNavigate, useParams } from 'react-router-dom';
import { Edit, Trash2 } from 'lucide-react';
import { deleteItem, getItem } from '../features/items/api/itemsApi';
import { useAsync } from '../features/items/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';

export function ItemDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: item, error, loading } = useAsync(() => getItem(id!), [id]);

  async function handleDelete() {
    if (!id || !window.confirm('Delete this item?')) {
      return;
    }

    await deleteItem(id);
    navigate('/items');
  }

  return (
    <section className="space-y-6">
      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {item ? (
        <>
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h1 className="text-2xl font-semibold text-slate-950">{item.title}</h1>
              <p className="mt-1 text-sm text-slate-600">Updated {new Date(item.updatedAt).toLocaleString()}</p>
            </div>
            <div className="flex gap-2">
              <Link
                to={`/items/${item.id}/edit`}
                className="inline-flex items-center gap-2 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-800 hover:bg-slate-50"
              >
                <Edit aria-hidden="true" className="h-4 w-4" />
                Edit
              </Link>
              <button
                type="button"
                onClick={handleDelete}
                className="inline-flex items-center gap-2 rounded-md border border-red-200 bg-white px-3 py-2 text-sm font-medium text-red-700 hover:bg-red-50"
              >
                <Trash2 aria-hidden="true" className="h-4 w-4" />
                Delete
              </button>
            </div>
          </div>
          <article className="rounded-md border border-slate-200 bg-white p-5">
            <p className="whitespace-pre-wrap text-slate-700">{item.description || 'No description'}</p>
          </article>
        </>
      ) : null}
    </section>
  );
}
