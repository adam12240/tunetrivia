// Utility functions extracted for testing
export function stripBrackets(s) {
  if (!s || typeof s !== 'string') return '';
  return s.replace(/\s*[([{][^)\]}]*[)\]}]\s*/g, ' ').replace(/\s+/g, ' ').trim();
}

export function shuffle(arr) {
  const a = Array.isArray(arr) ? [...arr] : [];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

