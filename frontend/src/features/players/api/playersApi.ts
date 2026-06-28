import { apiRequest } from '../../../shared/api/apiClient';
import type { Player, PlayerFormValues } from '../../../shared/types/player';

export function listPlayers(): Promise<Player[]> {
  return apiRequest<Player[]>('/players');
}

export function getPlayer(id: string): Promise<Player> {
  return apiRequest<Player>(`/players/${id}`);
}

export function createPlayer(values: PlayerFormValues): Promise<Player> {
  return apiRequest<Player>('/players', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}

export function updatePlayer(id: string, values: PlayerFormValues): Promise<Player> {
  return apiRequest<Player>(`/players/${id}`, {
    method: 'PUT',
    body: JSON.stringify(values),
  });
}

export function deletePlayer(id: string): Promise<void> {
  return apiRequest<void>(`/players/${id}`, { method: 'DELETE' });
}
