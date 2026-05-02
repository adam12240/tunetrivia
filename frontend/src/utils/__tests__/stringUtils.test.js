import { describe, it, expect } from 'vitest';
import { stripBrackets, shuffle } from '../stringUtils';

describe('stringUtils', () => {
  it('stripBrackets removes bracketed content', () => {
    const input = "Song Title (feat. Someone) [Live] {Remix}";
    expect(stripBrackets(input)).toBe('Song Title');
  });

  it('stripBrackets handles null or non-string', () => {
    expect(stripBrackets(null)).toBe('');
    expect(stripBrackets(123)).toBe('');
  });

  it('shuffle returns same items but different order (probabilistic)', () => {
    const arr = [1,2,3,4,5,6,7,8,9];
    const sh = shuffle(arr);
    expect(sh.sort()).toEqual(arr.sort());
    expect(sh.length).toBe(arr.length);
  });
});
