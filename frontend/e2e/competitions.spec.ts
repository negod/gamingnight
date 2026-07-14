import { expect, test } from '@playwright/test';
import { createApi } from './support/api';
import { adminCredentials, runId } from './support/env';
import { recordByText } from './support/ui';
import { ADMIN_STORAGE_STATE, USER_STORAGE_STATE } from './global-setup';

test.describe('normal user competition registration', () => {
  test.use({ storageState: USER_STORAGE_STATE });

  test('@competitions normal user can register and unregister for an open competition', async ({ page, request }) => {
    const prefix = `e2e-${runId()}-competitions-`;
    const api = await createApi(request, adminCredentials());
    await api.cleanupByPrefix(prefix);
    const game = await api.createGame(`${prefix}game`);
    const competition = await api.createOpenCompetition(`${prefix}open`, [game.id], []);

    try {
      await page.goto('/');
      await page.getByRole('link', { name: 'Competitions' }).click();
      await expect(page.getByText(competition.name)).toBeVisible();

      const row = recordByText(page, competition.name);
      await row.getByRole('button', { name: /register/i }).click();
      await expect(row.getByRole('button', { name: /registered/i })).toBeVisible();
      await row.getByRole('button', { name: /registered/i }).click();
      await expect(row.getByRole('button', { name: /register/i })).toBeVisible();
    } finally {
      await api.cleanupByPrefix(prefix);
    }
  });
});

test.describe('admin competition run', () => {
  test.use({ storageState: ADMIN_STORAGE_STATE });

  test('@competition-run @leaderboards admin can open a competition run view', async ({ page, request }) => {
    const prefix = `e2e-${runId()}-run-`;
    const api = await createApi(request, adminCredentials());
    await api.cleanupByPrefix(prefix);
    const game = await api.createGame(`${prefix}game`);
    const playerOne = await api.createPlayer(`${prefix}player-one`);
    const playerTwo = await api.createPlayer(`${prefix}player-two`);
    const teamOne = await api.createTeam(`${prefix}team-one`, [playerOne.id]);
    const teamTwo = await api.createTeam(`${prefix}team-two`, [playerTwo.id]);
    const competition = await api.createOpenCompetition(`${prefix}open`, [game.id], [teamOne.id, teamTwo.id]);

    try {
      await page.goto('/');
      await page.getByRole('link', { name: 'Competitions' }).click();
      await recordByText(page, competition.name).getByRole('button', { name: /run|open/i }).click();
      await page.getByRole('button', { name: 'Start competition' }).click();
      await expect(page.getByRole('heading', { name: /Matches/ })).toBeVisible();
      await page.getByRole('button', { name: 'Game', exact: true }).click();
      await expect(page.getByRole('heading', { name: /Teams/ })).toBeVisible();
      await page.getByRole('button', { name: 'Overall' }).click();
      await expect(page.getByRole('heading', { name: 'Overall — Teams' })).toBeVisible();
    } finally {
      await api.cleanupByPrefix(prefix);
    }
  });
});
