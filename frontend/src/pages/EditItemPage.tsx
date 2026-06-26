import { useNavigate, useParams } from 'react-router-dom';
import { getItem, updateItem } from '../features/items/api/itemsApi';
import { ItemForm } from '../features/items/components/ItemForm';
import { useAsync } from '../features/items/hooks/useAsync';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import type { ItemFormValues } from '../shared/types/item';

export function EditItemPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data, error, loading } = useAsync(() => getItem(id!), [id]);

  async function handleSubmit(values: ItemFormValues) {
    const item = await updateItem(id!, values);
    navigate(`/items/${item.id}`);
  }

  return (
    <section className="max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Edit item</h1>
      </div>
      {loading ? <LoadingMessage /> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {data ? (
        <div className="rounded-md border border-slate-200 bg-white p-5">
          <ItemForm
            submitLabel="Save changes"
            initialValues={{ title: data.title, description: data.description }}
            onSubmit={handleSubmit}
          />
        </div>
      ) : null}
    </section>
  );
}
