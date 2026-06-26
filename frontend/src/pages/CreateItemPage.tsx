import { useNavigate } from 'react-router-dom';
import { createItem } from '../features/items/api/itemsApi';
import { ItemForm } from '../features/items/components/ItemForm';
import type { ItemFormValues } from '../shared/types/item';

export function CreateItemPage() {
  const navigate = useNavigate();

  async function handleSubmit(values: ItemFormValues) {
    const item = await createItem(values);
    navigate(`/items/${item.id}`);
  }

  return (
    <section className="max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Create item</h1>
        <p className="mt-1 text-sm text-slate-600">Add a new item to the collection.</p>
      </div>
      <div className="rounded-md border border-slate-200 bg-white p-5">
        <ItemForm submitLabel="Create item" onSubmit={handleSubmit} />
      </div>
    </section>
  );
}
