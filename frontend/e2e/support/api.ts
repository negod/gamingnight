import { APIRequestContext, expect } from '@playwright/test';
import { adminCredentials, apiBaseUrl, E2ECredentials } from './env';

type LoginResponse = {
  token: string;
  user: {
    id: string;
    username: string;
    email: string | null;
    role: 'ADMIN' | 'USER';
    playerId: string;
    playerName: string;
  };
};

type Player = {
  id: string;
  name: string;
};

type Team = {
  id: string;
  name: string;
  playerIds: string[];
};

type Game = {
  id: string;
  name: string;
};

type Competition = {
  id: string;
  name: string;
  registrationOpen: boolean;
};

export type E2EApi = {
  token: string;
  login: LoginResponse;
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
  put<T>(path: string, body: unknown): Promise<T>;
  delete(path: string): Promise<void>;
  cleanupByPrefix(prefix: string): Promise<void>;
  createPlayer(name: string): Promise<Player>;
  createTeam(name: string, playerIds?: string[]): Promise<Team>;
  createGame(name: string): Promise<Game>;
  createOpenCompetition(name: string, gameIds: string[], teamIds: string[]): Promise<Competition>;
};

export async function loginViaApi(request: APIRequestContext, credentials: E2ECredentials): Promise<LoginResponse> {
  const response = await request.post(`${apiBaseUrl()}/auth/login`, { data: credentials });
  expect(response.ok(), await response.text()).toBeTruthy();
  return response.json() as Promise<LoginResponse>;
}

export async function createApi(request: APIRequestContext, credentials = adminCredentials()): Promise<E2EApi> {
  const login = await loginViaApi(request, credentials);
  const headers = {
    Authorization: `Bearer ${login.token}`,
  };

  async function checked<T>(method: 'get' | 'post' | 'put' | 'delete', path: string, body?: unknown): Promise<T> {
    const url = `${apiBaseUrl()}${path}`;
    const response = method === 'get' || method === 'delete'
      ? await request[method](url, { headers })
      : await request[method](url, { headers, data: body });
    expect(response.ok(), `${method.toUpperCase()} ${path}: ${await response.text()}`).toBeTruthy();
    if (response.status() === 204) {
      return undefined as T;
    }
    return response.json() as Promise<T>;
  }

  async function cleanupByPrefix(prefix: string): Promise<void> {
    const competitions = await checked<Competition[]>('get', '/competitions');
    for (const competition of competitions.filter((item) => item.name.startsWith(prefix))) {
      await checked<void>('delete', `/competitions/${competition.id}`);
    }

    const teams = await checked<Team[]>('get', '/teams');
    for (const team of teams.filter((item) => item.name.startsWith(prefix))) {
      await checked<void>('delete', `/teams/${team.id}`);
    }

    const games = await checked<Game[]>('get', '/games');
    for (const game of games.filter((item) => item.name.startsWith(prefix))) {
      await checked<void>('delete', `/games/${game.id}`);
    }

    const players = await checked<Player[]>('get', '/players');
    for (const player of players.filter((item) => item.name.startsWith(prefix))) {
      await checked<void>('delete', `/players/${player.id}`);
    }
  }

  return {
    token: login.token,
    login,
    get: (path) => checked('get', path),
    post: (path, body) => checked('post', path, body),
    put: (path, body) => checked('put', path, body),
    delete: (path) => checked('delete', path),
    cleanupByPrefix,
    createPlayer: (name) => checked<Player>('post', '/players', { name }),
    createTeam: (name, playerIds = []) => checked<Team>('post', '/teams', { name, playerIds }),
    createGame: (name) =>
      checked<Game>('post', '/games', {
        name,
        description: 'Created by production E2E tests',
        platform: null,
        genre: null,
        referenceUrl: null,
        isActive: true,
        matchType: 'FREE_FOR_ALL',
        participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 4, numberOfTeams: null, allowSubstitutes: false },
        resultType: 'SCORE',
        winnerRule: 'HIGHEST_VALUE_WINS',
        scoringRule: { type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 },
        tieBreakerRule: 'ALLOW_DRAW',
        validationRule: null,
        rotationRule: 'NONE',
        timeLimitRule: null,
        bonusRules: [],
      }),
    createOpenCompetition: (name, gameIds, teamIds) =>
      checked<Competition>('post', '/competitions', {
        name,
        date: new Date().toISOString().slice(0, 10),
        singleMatch: false,
        registrationOpen: true,
        gameIds,
        teamIds,
      }),
  };
}
