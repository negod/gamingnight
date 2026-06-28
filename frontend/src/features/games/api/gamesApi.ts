import { apiRequest } from '../../../shared/api/apiClient';
import type { Game, GameFormValues } from '../../../shared/types/game';

export function listGames(): Promise<Game[]> {
  return apiRequest<Game[]>('/games');
}

export function getGame(id: string): Promise<Game> {
  return apiRequest<Game>(`/games/${id}`);
}

export function createGame(values: GameFormValues): Promise<Game> {
  return apiRequest<Game>('/games', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}

export function updateGame(id: string, values: GameFormValues): Promise<Game> {
  return apiRequest<Game>(`/games/${id}`, {
    method: 'PUT',
    body: JSON.stringify(values),
  });
}

export function deleteGame(id: string): Promise<void> {
  return apiRequest<void>(`/games/${id}`, { method: 'DELETE' });
}
