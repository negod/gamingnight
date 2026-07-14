import { Link } from 'react-router-dom';
import { Pencil, ShieldCheck, Trash2 } from 'lucide-react';
import type { AppUser } from '../../../shared/types/user';

type UserListProps = {
  users: AppUser[];
  onDelete: (id: string) => void;
};

export function UserList({ users, onDelete }: UserListProps) {
  if (users.length === 0) {
    return <p className="text-sm text-slate-500">No users yet.</p>;
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-600">
          <tr>
            <th className="px-4 py-3 font-medium">Username</th>
            <th className="px-4 py-3 font-medium">Email</th>
            <th className="px-4 py-3 font-medium">Role</th>
            <th className="px-4 py-3 font-medium">Player callsign</th>
            <th className="px-4 py-3 font-medium">Created</th>
            <th className="px-4 py-3 font-medium sr-only">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {users.map((user) => (
            <tr key={user.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-medium text-slate-900">
                <span className="flex items-center gap-2">
                  <ShieldCheck aria-hidden="true" className="h-4 w-4 text-slate-400" />
                  {user.username}
                </span>
              </td>
              <td className="px-4 py-3 text-slate-600">{user.email || '-'}</td>
              <td className="px-4 py-3 text-slate-600">{user.role === 'ADMIN' ? 'Admin' : 'User'}</td>
              <td className="px-4 py-3 text-slate-600">{user.playerName}</td>
              <td className="px-4 py-3 text-slate-600">
                {new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(user.createdAt))}
              </td>
              <td className="px-4 py-3">
                <div className="flex justify-end gap-2">
                  <Link
                    to={`/users/${user.id}/edit`}
                    className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                  >
                    <Pencil aria-hidden="true" className="h-3.5 w-3.5" />
                    Edit
                  </Link>
                  <button
                    onClick={() => onDelete(user.id)}
                    className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-red-600 hover:bg-red-50"
                  >
                    <Trash2 aria-hidden="true" className="h-3.5 w-3.5" />
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
