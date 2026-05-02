import { vi, describe, it, expect } from 'vitest';
import React from 'react';
import { render } from '@testing-library/react';
import Quiz from '../Quiz.jsx';

// Mock the apiFetch used in Quiz
vi.mock('../../services/api', () => ({
  apiFetch: async (url, opts) => {
    if (url.startsWith('/deezer')) {
      return {
        ok: true,
        json: async () => ({ data: [], topArtists: [] }),
      };
    }
    return { ok: false };
  },
}));

describe('Quiz component', () => {
  it('renders without crashing', () => {
    const { container } = render(<Quiz user={null} />);
    expect(container).toBeTruthy();
  });
});
