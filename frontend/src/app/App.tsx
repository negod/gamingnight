import { Link, Navigate, NavLink, Route, Routes } from 'react-router-dom';
import { ClipboardList, Gamepad2, LogOut, ShieldCheck, Trophy, UserRound, Users } from 'lucide-react';
import { GamesPage } from '../pages/GamesPage';
import { CreateGamePage } from '../pages/CreateGamePage';
import { EditGamePage } from '../pages/EditGamePage';
import { TeamsPage } from '../pages/TeamsPage';
import { CreateTeamPage } from '../pages/CreateTeamPage';
import { EditTeamPage } from '../pages/EditTeamPage';
import { PlayersPage } from '../pages/PlayersPage';
import { CreatePlayerPage } from '../pages/CreatePlayerPage';
import { EditPlayerPage } from '../pages/EditPlayerPage';
import { UsersPage } from '../pages/UsersPage';
import { CreateUserPage } from '../pages/CreateUserPage';
import { EditUserPage } from '../pages/EditUserPage';
import { CurrentUserPage } from '../pages/CurrentUserPage';
import { LoginPage } from '../pages/LoginPage';
import { CompetitionsPage } from '../pages/CompetitionsPage';
import { CreateCompetitionPage } from '../pages/CreateCompetitionPage';
import { EditCompetitionPage } from '../pages/EditCompetitionPage';
import { CompetitionRunPage } from '../pages/CompetitionRunPage';
import { HealthStatus } from '../shared/components/HealthStatus';
import { useAuth } from '../shared/auth/AuthContext';

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-1.5 text-sm font-medium px-2 py-1 rounded-md transition-colors ${
    isActive ? 'text-teal-700 bg-teal-50' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
  }`;

export function App() {
  const auth = useAuth();
  const user = auth.user;

  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  const admin = user.role === 'ADMIN';

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
            {admin ? (
              <>
                <NavLink to="/games" className={navLinkClass}>
                  <Gamepad2 aria-hidden="true" className="h-4 w-4" />
                  Games
                </NavLink>
                <NavLink to="/teams" className={navLinkClass}>
                  <Users aria-hidden="true" className="h-4 w-4" />
                  Teams
                </NavLink>
                <NavLink to="/players" className={navLinkClass}>
                  <UserRound aria-hidden="true" className="h-4 w-4" />
                  Players
                </NavLink>
              </>
            ) : null}
            <NavLink to={admin ? '/users' : '/users/me'} className={navLinkClass}>
              <ShieldCheck aria-hidden="true" className="h-4 w-4" />
              {admin ? 'Users' : 'My user'}
            </NavLink>
          </nav>
          <div className="flex items-center gap-3">
            <HealthStatus />
            <button
              type="button"
              onClick={auth.logout}
              className="inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-sm font-medium text-slate-600 hover:bg-slate-100 hover:text-slate-900"
            >
              <LogOut aria-hidden="true" className="h-4 w-4" />
              Sign out
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8">
        <Routes>
          <Route path="/" element={<Navigate to={admin ? '/competitions' : '/users/me'} replace />} />
          <Route path="/login" element={<Navigate to={admin ? '/competitions' : '/users/me'} replace />} />
          <Route path="/competitions" element={<CompetitionsPage />} />
          {admin ? <Route path="/competitions/new" element={<CreateCompetitionPage />} /> : null}
          {admin ? <Route path="/competitions/:id/edit" element={<EditCompetitionPage />} /> : null}
          <Route path="/competitions/:id/run" element={<CompetitionRunPage />} />
          {admin ? <Route path="/games" element={<GamesPage />} /> : null}
          {admin ? <Route path="/games/new" element={<CreateGamePage />} /> : null}
          {admin ? <Route path="/games/:id/edit" element={<EditGamePage />} /> : null}
          {admin ? <Route path="/teams" element={<TeamsPage />} /> : null}
          {admin ? <Route path="/teams/new" element={<CreateTeamPage />} /> : null}
          {admin ? <Route path="/teams/:id/edit" element={<EditTeamPage />} /> : null}
          {admin ? <Route path="/players" element={<PlayersPage />} /> : null}
          {admin ? <Route path="/players/new" element={<CreatePlayerPage />} /> : null}
          {admin ? <Route path="/players/:id/edit" element={<EditPlayerPage />} /> : null}
          {admin ? <Route path="/users" element={<UsersPage />} /> : null}
          <Route path="/users/me" element={<CurrentUserPage />} />
          {admin ? <Route path="/users/new" element={<CreateUserPage />} /> : null}
          {admin ? <Route path="/users/:id/edit" element={<EditUserPage />} /> : null}
          <Route path="*" element={<Navigate to={admin ? '/competitions' : '/users/me'} replace />} />
        </Routes>
      </main>
    </div>
  );
}
