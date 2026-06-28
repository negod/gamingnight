import { apiRequest } from '../../../shared/api/apiClient';
import type { Team, TeamFormValues } from '../../../shared/types/team';
export { listPlayers } from '../../players/api/playersApi';

export function listTeams(): Promise<Team[]> {
  return apiRequest<Team[]>('/teams');
}

export function getTeam(id: string): Promise<Team> {
  return apiRequest<Team>(`/teams/${id}`);
}

export function createTeam(values: TeamFormValues): Promise<Team> {
  return apiRequest<Team>('/teams', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}

export function updateTeam(id: string, values: TeamFormValues): Promise<Team> {
  return apiRequest<Team>(`/teams/${id}`, {
    method: 'PUT',
    body: JSON.stringify(values),
  });
}

export function deleteTeam(id: string): Promise<void> {
  return apiRequest<void>(`/teams/${id}`, { method: 'DELETE' });
}
