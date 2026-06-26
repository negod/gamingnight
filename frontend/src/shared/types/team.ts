export type Team = {
  id: string;
  name: string;
  playerIds: string[];
  createdAt: string;
  updatedAt: string;
};

export type TeamFormValues = {
  name: string;
  playerIds: string[];
};
