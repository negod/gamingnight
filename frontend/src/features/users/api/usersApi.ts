import { apiRequest } from '../../../shared/api/apiClient';
import type { AppUser, CurrentUserFormValues, UserFormValues } from '../../../shared/types/user';
export { listPlayers } from '../../players/api/playersApi';

export function listUsers(): Promise<AppUser[]> {
  return apiRequest<AppUser[]>('/users');
}

export function getUser(id: string): Promise<AppUser> {
  return apiRequest<AppUser>(`/users/${id}`);
}

export function getCurrentUser(): Promise<AppUser> {
  return apiRequest<AppUser>('/users/me');
}

export function updateCurrentUser(values: CurrentUserFormValues): Promise<AppUser> {
  return apiRequest<AppUser>('/users/me', {
    method: 'PUT',
    body: JSON.stringify(values),
  });
}

export function createUser(values: UserFormValues): Promise<AppUser> {
  return apiRequest<AppUser>('/users', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}

export function updateUser(id: string, values: UserFormValues): Promise<AppUser> {
  return apiRequest<AppUser>(`/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(values),
  });
}

export function deleteUser(id: string): Promise<void> {
  return apiRequest<void>(`/users/${id}`, { method: 'DELETE' });
}
