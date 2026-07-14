export type Competition = {
  id: string;
  name: string;
  date: string;
  singleMatch: boolean;
  registrationOpen: boolean;
  started: boolean;
  gameIds: string[];
  teamIds: string[];
  registeredPlayerIds: string[];
  createdAt: string;
  updatedAt: string;
};

export type CompetitionFormValues = {
  name: string;
  date: string;
  singleMatch: boolean;
  registrationOpen: boolean;
  gameIds: string[];
  teamIds: string[];
};
