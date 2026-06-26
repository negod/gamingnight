import { apiRequest } from '../../../shared/api/apiClient';
import type {
  GameTeamLeaderboard,
  GamePlayerLeaderboard,
  TotalTeamLeaderboardRow,
  TotalPlayerLeaderboardRow,
} from '../../../shared/types/leaderboard';

export function getGameTeamLeaderboard(
  competitionId: string,
  gameId: string,
): Promise<GameTeamLeaderboard> {
  return apiRequest<GameTeamLeaderboard>(
    `/competitions/${competitionId}/leaderboard/games/${gameId}/teams`,
  );
}

export function getGamePlayerLeaderboard(
  competitionId: string,
  gameId: string,
): Promise<GamePlayerLeaderboard> {
  return apiRequest<GamePlayerLeaderboard>(
    `/competitions/${competitionId}/leaderboard/games/${gameId}/players`,
  );
}

export function getTotalTeamLeaderboard(competitionId: string): Promise<TotalTeamLeaderboardRow[]> {
  return apiRequest<TotalTeamLeaderboardRow[]>(
    `/competitions/${competitionId}/leaderboard/teams`,
  );
}

export function getTotalPlayerLeaderboard(
  competitionId: string,
): Promise<TotalPlayerLeaderboardRow[]> {
  return apiRequest<TotalPlayerLeaderboardRow[]>(
    `/competitions/${competitionId}/leaderboard/players`,
  );
}
