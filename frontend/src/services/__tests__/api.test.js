import { describe, it, expect, vi, afterEach } from 'vitest';
import { apiFetch } from '../../services/api';

describe('apiFetch', () => {
  const originalFetch = globalThis.fetch;
  afterEach(() => { globalThis.fetch = originalFetch; vi.restoreAllMocks(); });

  it('prefixes relative paths with backend base URL', async () => {
    globalThis.fetch = vi.fn(async (url, opts) => ({ ok: true, status: 200, json: async () => ({}) }));
    await apiFetch('/test/path', { method: 'GET' });
    expect(globalThis.fetch).toHaveBeenCalled();
    const called = globalThis.fetch.mock.calls[0][0];
    expect(called).toMatch(/http:\/\/localhost:3002\/test\/path$/);
  });

  it('passes through absolute URLs unchanged', async () => {
    globalThis.fetch = vi.fn(async (url, opts) => ({ ok: true, status: 200, json: async () => ({}) }));
    const full = 'https://example.com/api/ok';
    await apiFetch(full, { method: 'POST' });
    const called = globalThis.fetch.mock.calls[0][0];
    expect(called).toBe(full);
  });
});
