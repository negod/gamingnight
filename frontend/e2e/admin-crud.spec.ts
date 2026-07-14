import { expect, test } from '@playwright/test';
import { createApi } from './support/api';
import { adminCredentials, runId } from './support/env';
import { login, recordByText } from './support/ui';

test('@players admin can create, edit, and delete an isolated player', async ({ page, request }) => {
  const prefix = `e2e-${runId()}-players-`;
  const api = await createApi(request);
  await api.cleanupByPrefix(prefix);

  await login(page, adminCredentials());
  await page.getByRole('link', { name: 'Players' }).click();
  await page.getByRole('button', { name: 'New player' }).click();
  await page.getByLabel('Player name').fill(`${prefix}one`);
  await page.getByRole('button', { name: 'Create player' }).click();
  await expect(page.getByText(`${prefix}one`)).toBeVisible();

  await recordByText(page, `${prefix}one`).getByRole('button', { name: 'Edit' }).click();
  await page.getByLabel('Player name').fill(`${prefix}two`);
  await page.getByRole('button', { name: 'Save changes' }).click();
  await expect(page.getByText(`${prefix}two`)).toBeVisible();

  page.on('dialog', (dialog) => dialog.accept());
  await recordByText(page, `${prefix}two`).getByRole('button', { name: 'Delete' }).click();
  await expect(page.getByText(`${prefix}two`)).toHaveCount(0);
});

test('@games admin can create, edit, and delete an isolated game', async ({ page, request }) => {
  const prefix = `e2e-${runId()}-games-`;
  const api = await createApi(request);
  await api.cleanupByPrefix(prefix);

  await login(page, adminCredentials());
  await page.getByRole('link', { name: 'Games' }).click();
  await page.getByRole('button', { name: 'New game' }).click();
  await page.getByLabel('Game name').fill(`${prefix}one`);
  await page.getByRole('button', { name: 'Create game' }).click();
  await expect(page.getByText(`${prefix}one`)).toBeVisible();

  await recordByText(page, `${prefix}one`).getByRole('button', { name: 'Edit' }).click();
  await page.getByLabel('Game name').fill(`${prefix}two`);
  await page.getByRole('button', { name: 'Save changes' }).click();
  await expect(page.getByText(`${prefix}two`)).toBeVisible();

  page.on('dialog', (dialog) => dialog.accept());
  await recordByText(page, `${prefix}two`).getByRole('button', { name: 'Delete' }).click();
  await expect(page.getByText(`${prefix}two`)).toHaveCount(0);
});

test('@teams admin can create, edit, and delete an isolated team', async ({ page, request }) => {
  const prefix = `e2e-${runId()}-teams-`;
  const api = await createApi(request);
  await api.cleanupByPrefix(prefix);
  const player = await api.createPlayer(`${prefix}player`);

  await login(page, adminCredentials());
  await page.getByRole('link', { name: 'Teams' }).click();
  await page.getByRole('button', { name: 'New team' }).click();
  await page.getByLabel('Team name').fill(`${prefix}one`);
  await page.getByPlaceholder('Search players').fill(player.name);
  await page.getByLabel(player.name).check();
  await page.getByRole('button', { name: 'Create team' }).click();
  await expect(page.getByText(`${prefix}one`)).toBeVisible();

  await recordByText(page, `${prefix}one`).getByRole('button', { name: 'Edit' }).click();
  await page.getByLabel('Team name').fill(`${prefix}two`);
  await page.getByRole('button', { name: 'Save changes' }).click();
  await expect(page.getByText(`${prefix}two`)).toBeVisible();

  page.on('dialog', (dialog) => dialog.accept());
  await recordByText(page, `${prefix}two`).getByRole('button', { name: 'Delete' }).click();
  await expect(page.getByText(`${prefix}two`)).toHaveCount(0);

  await api.cleanupByPrefix(prefix);
});
