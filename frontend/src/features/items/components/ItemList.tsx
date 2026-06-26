import { Link } from 'react-router-dom';
import type { Item } from '../../../shared/types/item';

type ItemListProps = {
  items: Item[];
};

export function ItemList({ items }: ItemListProps) {
  if (items.length === 0) {
    return <p className="rounded-md border border-slate-200 bg-white px-4 py-6 text-slate-600">No items yet.</p>;
  }

  return (
    <div className="overflow-hidden rounded-md border border-slate-200 bg-white">
      <ul className="divide-y divide-slate-200">
        {items.map((item) => (
          <li key={item.id}>
            <Link to={`/items/${item.id}`} className="block px-4 py-4 hover:bg-slate-50">
              <h2 className="font-medium text-slate-950">{item.title}</h2>
              <p className="mt-1 line-clamp-2 text-sm text-slate-600">{item.description || 'No description'}</p>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
