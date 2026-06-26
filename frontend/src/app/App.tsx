import { Link, Navigate, NavLink, Route, Routes } from 'react-router-dom';
import { ClipboardList, Trophy, Users } from 'lucide-react';
import { CreateItemPage } from '../pages/CreateItemPage';
import { EditItemPage } from '../pages/EditItemPage';
import { ItemDetailPage } from '../pages/ItemDetailPage';
import { ItemsPage } from '../pages/ItemsPage';
import { TeamsPage } from '../pages/TeamsPage';
import { CreateTeamPage } from '../pages/CreateTeamPage';
import { EditTeamPage } from '../pages/EditTeamPage';
import { CompetitionsPage } from '../pages/CompetitionsPage';
import { CreateCompetitionPage } from '../pages/CreateCompetitionPage';
import { EditCompetitionPage } from '../pages/EditCompetitionPage';
import { CompetitionRunPage } from '../pages/CompetitionRunPage';
import { HealthStatus } from '../shared/components/HealthStatus';

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-1.5 text-sm font-medium px-2 py-1 rounded-md transition-colors ${
    isActive ? 'text-teal-700 bg-teal-50' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
  }`;

export function App() {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <Link to="/competitions" className="flex items-center gap-2 text-lg font-semibold text-slate-950">
            <ClipboardList aria-hidden="true" className="h-5 w-5 text-teal-700" />
            Gaming Night
          </Link>
          <nav className="flex items-center gap-1">
            <NavLink to="/competitions" className={navLinkClass}>
              <Trophy aria-hidden="true" className="h-4 w-4" />
              Competitions
            </NavLink>
            <NavLink to="/teams" className={navLinkClass}>
              <Users aria-hidden="true" className="h-4 w-4" />
              Teams
            </NavLink>
            <NavLink to="/items" className={navLinkClass}>
              <ClipboardList aria-hidden="true" className="h-4 w-4" />
              Items
            </NavLink>
          </nav>
          <HealthStatus />
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8">
        <Routes>
          <Route path="/" element={<Navigate to="/competitions" replace />} />
          <Route path="/competitions" element={<CompetitionsPage />} />
          <Route path="/competitions/new" element={<CreateCompetitionPage />} />
          <Route path="/competitions/:id/edit" element={<EditCompetitionPage />} />
          <Route path="/competitions/:id/run" element={<CompetitionRunPage />} />
          <Route path="/teams" element={<TeamsPage />} />
          <Route path="/teams/new" element={<CreateTeamPage />} />
          <Route path="/teams/:id/edit" element={<EditTeamPage />} />
          <Route path="/items" element={<ItemsPage />} />
          <Route path="/items/new" element={<CreateItemPage />} />
          <Route path="/items/:id" element={<ItemDetailPage />} />
          <Route path="/items/:id/edit" element={<EditItemPage />} />
        </Routes>
      </main>
    </div>
  );
}
