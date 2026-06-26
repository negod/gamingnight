import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { ClipboardList } from 'lucide-react';
import { CreateItemPage } from '../pages/CreateItemPage';
import { EditItemPage } from '../pages/EditItemPage';
import { ItemDetailPage } from '../pages/ItemDetailPage';
import { ItemsPage } from '../pages/ItemsPage';
import { HealthStatus } from '../shared/components/HealthStatus';

export function App() {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <Link to="/items" className="flex items-center gap-2 text-lg font-semibold text-slate-950">
            <ClipboardList aria-hidden="true" className="h-5 w-5 text-teal-700" />
            Gaming Night
          </Link>
          <HealthStatus />
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8">
        <Routes>
          <Route path="/" element={<Navigate to="/items" replace />} />
          <Route path="/items" element={<ItemsPage />} />
          <Route path="/items/new" element={<CreateItemPage />} />
          <Route path="/items/:id" element={<ItemDetailPage />} />
          <Route path="/items/:id/edit" element={<EditItemPage />} />
        </Routes>
      </main>
    </div>
  );
}
