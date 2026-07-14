import { useEffect, useState } from 'react';
import type { Match } from '../../../shared/types/match';
import type { Team } from '../../../shared/types/team';
import type { Player } from '../../../shared/types/player';
import { getTeam, getPlayer } from '../api/competitionRunApi';

export type PlayerRow = {
  playerId: string;
  teamId: string;
  playerName: string;
  teamName: string;
  value: string;
};

type UseMatchDetailsResult = {
  rows: PlayerRow[];
  loading: boolean;
  error: string | null;
};

export function useMatchDetails(match: Match | null): UseMatchDetailsResult {
  const [rows, setRows] = useState<PlayerRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!match) {
      setRows([]);
      setLoading(false);
      setError(null);
      return;
    }

    let active = true;
    setLoading(true);
    setError(null);

    Promise.all([getTeam(match.homeTeamId), getTeam(match.awayTeamId)])
      .then(async ([homeTeam, awayTeam]: [Team, Team]) => {
        const allPlayerIds = [
          ...homeTeam.playerIds.map((id: string) => ({ id, team: homeTeam })),
          ...awayTeam.playerIds.map((id: string) => ({ id, team: awayTeam })),
        ];

        const players = await Promise.all(
          allPlayerIds.map(({ id }) =>
            getPlayer(id).catch((): Player => ({ id, name: id, createdAt: '', updatedAt: '' })),
          ),
        );

        if (!active) return;

        const existingByPlayer = new Map(match.results.map((r) => [r.playerId, r.value]));

        const builtRows: PlayerRow[] = allPlayerIds.map(({ id, team }, index) => ({
          playerId: id,
          teamId: team.id,
          playerName: players[index].name,
          teamName: team.name,
          value: String(existingByPlayer.get(id) ?? ''),
        }));

        setRows(builtRows);
        setLoading(false);
      })
      .catch((err: unknown) => {
        if (active) {
          setError(err instanceof Error ? err.message : 'Failed to load teams');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [match?.awayTeamId, match?.homeTeamId, match?.id, match?.results]);

  return { rows, loading, error };
}
