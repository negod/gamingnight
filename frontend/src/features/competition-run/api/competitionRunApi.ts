import { apiRequest } from '../../../shared/api/apiClient';
import type { Competition } from '../../../shared/types/competition';
import type { Game } from '../../../shared/types/game';
import type { Match, EnterResultsPayload } from '../../../shared/types/match';
import type { Team } from '../../../shared/types/team';
import type { Player } from '../../../shared/types/player';

export function startCompetition(competitionId: string): Promise<Competition> {
  return apiRequest<Competition>(`/competitions/${competitionId}/start`, { method: 'POST' });
}

export function getMatches(competitionId: string, gameId: string): Promise<Match[]> {
  return apiRequest<Match[]>(`/competitions/${competitionId}/games/${gameId}/matches`);
}

export function enterResults(
  competitionId: string,
  matchId: string,
  payload: EnterResultsPayload,
): Promise<Match> {
  return apiRequest<Match>(`/competitions/${competitionId}/matches/${matchId}/results`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function getCompetition(id: string): Promise<Competition> {
  return apiRequest<Competition>(`/competitions/${id}`);
}

export function getGame(id: string): Promise<Game> {
  return apiRequest<Game>(`/games/${id}`);
}

export function getTeam(id: string): Promise<Team> {
  return apiRequest<Team>(`/teams/${id}`);
}

export function getPlayer(id: string): Promise<Player> {
  return apiRequest<Player>(`/players/${id}`);
}
