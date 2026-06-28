export type PlayerResult = {
  playerId: string;
  teamId: string;
  playerName: string;
  teamName: string;
  value: number;
};

export type Match = {
  id: string;
  competitionId: string;
  gameId: string;
  homeTeamId: string;
  homeTeamName: string;
  awayTeamId: string;
  awayTeamName: string;
  completed: boolean;
  results: PlayerResult[];
  createdAt: string;
  updatedAt: string;
};

export type EnterResultsPayload = {
  results: Array<{ playerId: string; teamId: string; value: number }>;
};
