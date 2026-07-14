export type E2ECredentials = {
  username: string;
  password: string;
};

export function requiredEnv(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new Error(`${name} must be set for production E2E tests`);
  }
  return value;
}

export function apiBaseUrl(): string {
  return requiredEnv('E2E_API_BASE_URL').replace(/\/$/, '');
}

export function adminCredentials(): E2ECredentials {
  return {
    username: requiredEnv('E2E_ADMIN_USERNAME'),
    password: requiredEnv('E2E_ADMIN_PASSWORD'),
  };
}

export function userCredentials(): E2ECredentials {
  return {
    username: requiredEnv('E2E_USER_USERNAME'),
    password: requiredEnv('E2E_USER_PASSWORD'),
  };
}

export function runId(): string {
  return process.env.GITHUB_RUN_ID ?? `${Date.now()}`;
}
