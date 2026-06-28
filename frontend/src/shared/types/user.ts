export type UserRole = 'ADMIN' | 'USER';

export type AppUser = {
  id: string;
  username: string;
  email: string | null;
  role: UserRole;
  playerId: string;
  playerName: string;
  createdAt: string;
  updatedAt: string;
};

export type UserFormValues = {
  username: string;
  email?: string;
  password?: string;
  role: UserRole;
  playerId: string;
};

export type LoginValues = {
  username: string;
  password: string;
};

export type SignupValues = {
  username: string;
  email: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  user: AppUser;
};
