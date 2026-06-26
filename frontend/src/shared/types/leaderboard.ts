export type GameTeamLeaderboardRow = {
  rank: number;
  teamId: string;
  teamName: string;
  value: number;
};

export type GameTeamLeaderboard = {
  columnHeader: string;
  rows: GameTeamLeaderboardRow[];
};

export type GamePlayerLeaderboardRow = {
  rank: number;
  playerId: string;
  playerName: string;
  value: number;
};

export type GamePlayerLeaderboard = {
  columnHeader: string;
  rows: GamePlayerLeaderboardRow[];
};

export type TotalTeamLeaderboardRow = {
  rank: number;
  teamId: string;
  teamName: string;
  points: number;
};

export type TotalPlayerLeaderboardRow = {
  rank: number;
  playerId: string;
  playerName: string;
  points: number;
};
