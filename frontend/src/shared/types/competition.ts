export type Competition = {
  id: string;
  name: string;
  date: string;
  singleMatch: boolean;
  started: boolean;
  gameIds: string[];
  teamIds: string[];
  createdAt: string;
  updatedAt: string;
};

export type CompetitionFormValues = {
  name: string;
  date: string;
  singleMatch: boolean;
  gameIds: string[];
  teamIds: string[];
};
