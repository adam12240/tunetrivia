import { describe, it, expect, vi } from 'vitest';
import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import Quiz from '../Quiz.jsx';
import * as api from '../../services/api';

vi.mock('../../services/api');

describe('Quiz Last.fm import flow', () => {
  it('disables Start Quiz when Last.fm username is empty', async () => {
    const { container } = render(<Quiz user={null} />);
    // scope queries to the first quiz-search block to avoid duplicates
    const firstSearch = container.querySelector('.quiz-search');
    const modeSelect = within(firstSearch).getByLabelText(/Game Mode/i);
    fireEvent.change(modeSelect, { target: { value: 'lastfm' } });
    // Start Quiz button inside lastfm mode should be present and disabled because username is empty
    const startBtn = within(firstSearch).getByText(/Start Quiz/i);
    expect(startBtn).toBeTruthy();
    expect(startBtn.disabled).toBe(true);
  });

  it('imports lastfm tracks when apiFetch returns data', async () => {
    const mockTracks = [
      { title: 'T1', artist: { name: 'A1' }, preview: 'p1' },
      { title: 'T2', artist: { name: 'A2' }, preview: 'p2' },
      { title: 'T3', artist: { name: 'A3' }, preview: 'p3' },
      { title: 'T4', artist: { name: 'A4' }, preview: 'p4' }
    ];
    api.apiFetch.mockImplementation(async (path, opts) => ({ ok: true, json: async () => mockTracks }));

    const { container } = render(<Quiz user={null} />);
    const firstSearch = container.querySelector('.quiz-search');
    const modeSelect = within(firstSearch).getByLabelText(/Game Mode/i);
    fireEvent.change(modeSelect, { target: { value: 'lastfm' } });

    const usernameInput = within(firstSearch).getByPlaceholderText(/Last.fm username/i);
    fireEvent.change(usernameInput, { target: { value: 'someuser' } });

    const startBtn = within(firstSearch).getByText(/Start Quiz/i);
    // now it should be enabled
    expect(startBtn.disabled).toBe(false);
    fireEvent.click(startBtn);

    // Wait for Submit button to appear (quiz question rendered)
    await waitFor(() => {
      // use getByText inside waitFor (throws until found) and avoid jest-dom matchers
      const btn = screen.getByText(/Submit/i);
      expect(btn).toBeTruthy();
    }, { timeout: 3000 });
  });
});
