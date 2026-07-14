import { expect, test } from '@playwright/test';
import { createApi } from './support/api';
import { adminCredentials, userCredentials } from './support/env';
import { login } from './support/ui';

test('@users admin can view the users page', async ({ page }) => {
  await login(page, adminCredentials());

  await page.getByRole('link', { name: 'Users' }).click();
  await expect(page.getByRole('heading', { name: 'Users' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'New user' })).toBeVisible();
});

test('@users normal user can edit email and Player callsign without role access', async ({ page, request }) => {
  const api = await createApi(request, userCredentials());
  const original = await api.get<{ email: string | null; playerName: string }>('/users/me');
  const changedEmail = `e2e-${Date.now()}@example.invalid`;
  const changedCallsign = `e2e-${Date.now()}-user`;

  await login(page, userCredentials());
  await page.getByRole('link', { name: 'My user' }).click();
  await expect(page.getByRole('heading', { name: 'My user' })).toBeVisible();
  await expect(page.getByLabel('Role')).toHaveCount(0);

  try {
    await page.getByLabel('Email').fill(changedEmail);
    await page.getByLabel('Player callsign').fill(changedCallsign);
    await page.getByRole('button', { name: 'Save changes' }).click();
    await expect(page.getByLabel('Email')).toHaveValue(changedEmail);
    await expect(page.getByLabel('Player callsign')).toHaveValue(changedCallsign);
  } finally {
    await api.put('/users/me', {
      email: original.email ?? '',
      playerCallsign: original.playerName,
    });
  }
});
