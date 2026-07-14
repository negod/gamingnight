import { FullConfig, request } from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { adminCredentials, apiBaseUrl, E2ECredentials, userCredentials } from './support/env';

const currentDir = path.dirname(fileURLToPath(import.meta.url));

export const AUTH_DIR = path.join(currentDir, '.auth');
export const ADMIN_STORAGE_STATE = path.join(AUTH_DIR, 'admin.json');
export const USER_STORAGE_STATE = path.join(AUTH_DIR, 'user.json');

type LoginResponse = {
  token: string;
  user: unknown;
};

async function authenticate(origin: string, credentials: E2ECredentials, outputFile: string): Promise<void> {
  const api = await request.newContext();
  try {
    const response = await api.post(`${apiBaseUrl()}/auth/login`, { data: credentials });
    if (!response.ok()) {
      throw new Error(`Login failed for ${credentials.username}: ${response.status()} ${await response.text()}`);
    }
    const login = (await response.json()) as LoginResponse;
    fs.writeFileSync(
      outputFile,
      JSON.stringify(
        {
          cookies: [],
          origins: [
            {
              origin,
              localStorage: [
                { name: 'gaming-night-token', value: login.token },
                { name: 'gaming-night-user', value: JSON.stringify(login.user) },
              ],
            },
          ],
        },
        null,
        2,
      ),
    );
  } finally {
    await api.dispose();
  }
}

export default async function globalSetup(config: FullConfig): Promise<void> {
  fs.mkdirSync(AUTH_DIR, { recursive: true });
  const baseURL = (config.projects[0]?.use.baseURL as string | undefined) ?? 'http://localhost:5173';
  const origin = new URL(baseURL).origin;
  await authenticate(origin, adminCredentials(), ADMIN_STORAGE_STATE);
  await authenticate(origin, userCredentials(), USER_STORAGE_STATE);
}
