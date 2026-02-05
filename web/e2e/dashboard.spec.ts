import { test, expect } from '@playwright/test';

test('loads and shows login button', async ({ page }) => {
  // Mock config endpoint
  await page.route('**/actuator/info', async route => {
    const json = { auth: { "issuer-uri": "http://mock-oidc" } };
    await route.fulfill({
      json,
      headers: {
        'Access-Control-Allow-Origin': '*'
      }
    });
  });

  await page.goto('/');

  await expect(page).toHaveTitle(/PortURL/);
  await expect(page.getByRole('button', { name: /Login/i })).toBeVisible();

  await page.screenshot({ path: 'screenshot.png' });
});
