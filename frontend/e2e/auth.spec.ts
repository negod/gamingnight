import { expect, test } from '@playwright/test';
import { userCredentials } from './support/env';
import { login, logout } from './support/ui';

test('@smoke @auth user can sign in and reach the main app', async ({ page }) => {
  await login(page, userCredentials());

  await expect(page.getByRole('link', { name: 'Competitions' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'My user' })).toBeVisible();
  await page.getByRole('link', { name: 'Competitions' }).click();
  await expect(page.getByRole('heading', { name: 'Competitions' })).toBeVisible();
});

test('@auth rejects invalid credentials', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Username').fill('not-a-real-e2e-user');
  await page.getByLabel('Password').fill('wrong-password');
  await page.getByRole('button', { name: 'Sign in' }).click();

  await expect(page.getByText(/invalid|failed|unable|bad credentials/i)).toBeVisible();
});

test('@auth user can sign out', async ({ page }) => {
  await login(page, userCredentials());
  await logout(page);
});
