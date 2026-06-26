import { apiRequest } from '../../../shared/api/apiClient';
import type { Competition, CompetitionFormValues } from '../../../shared/types/competition';
import type { Game } from '../../../shared/types/game';
import type { Team } from '../../../shared/types/team';

export function listCompetitions(): Promise<Competition[]> {
  return apiRequest<Competition[]>('/competitions');
}

export function getCompetition(id: string): Promise<Competition> {
  return apiRequest<Competition>(`/competitions/${id}`);
}

export function createCompetition(values: CompetitionFormValues): Promise<Competition> {
  return apiRequest<Competition>('/competitions', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}

export function updateCompetition(id: string, values: CompetitionFormValues): Promise<Competition> {
  return apiRequest<Competition>(`/competitions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(values),
  });
}

export function deleteCompetition(id: string): Promise<void> {
  return apiRequest<void>(`/competitions/${id}`, { method: 'DELETE' });
}

export function generateTeams(competitionId: string, playerIds: string[], teamSize: number): Promise<Competition> {
  return apiRequest<Competition>(`/competitions/${competitionId}/generate-teams`, {
    method: 'POST',
    body: JSON.stringify({ playerIds, teamSize }),
  });
}

export function listGames(): Promise<Game[]> {
  return apiRequest<Game[]>('/games');
}

export function listTeams(): Promise<Team[]> {
  return apiRequest<Team[]>('/teams');
}
