import { expect, Page } from '@playwright/test';
import { E2ECredentials } from './env';

export async function login(page: Page, credentials: E2ECredentials): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Username').fill(credentials.username);
  await page.getByLabel('Password').fill(credentials.password);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page.getByRole('button', { name: 'Sign out' })).toBeVisible();
}

export async function logout(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Sign out' }).click();
  await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
}

export function recordByText(page: Page, text: string) {
  const row = page.locator('tr').filter({ hasText: text }).first();
  return row.or(page.locator('article, li').filter({ hasText: text }).first()).first();
}
