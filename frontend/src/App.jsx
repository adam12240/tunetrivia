import './App.css';
import { useEffect, useState } from 'react';
import GoogleLogin from './components/GoogleLogin.jsx';
import Header from './components/Header.jsx';
import ProfileMenu from './components/ProfileMenu.jsx';
import Quiz from './components/Quiz.jsx';
import { apiFetch } from './services/api';

export default function App() {
  // Minimal App state: auth/profile & stats. The full quiz lives in components/Quiz.jsx
  const backendUrl = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_BACKEND_URL) ? import.meta.env.VITE_BACKEND_URL : 'http://localhost:3002';
  const [user, setUser] = useState(null);
  const [profileOpen, setProfileOpen] = useState(false);

  const [myStats, setMyStats] = useState([]);
  const [statsLoading, setStatsLoading] = useState(false);
  const [statsError, setStatsError] = useState(null);
  const [mySummary, setMySummary] = useState(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [, setSummaryError] = useState(null);

  const fetchMe = async () => {
    try {
      const res = await apiFetch('/api/auth/me');
      if (!res.ok) {
        setUser(null);
        return null;
      }
      const json = await res.json();
      setUser(json);
      return json;
    } catch {
      setUser(null);
      return null;
    }
  };

  const fetchMyStats = async () => {
    setStatsLoading(true);
    setStatsError(null);
    try {
      const res = await apiFetch('/api/stats/me');
      if (!res.ok) {
        setMyStats([]);
        setStatsError('Could not load stats.');
        return;
      }
      const json = await res.json();
      setMyStats(Array.isArray(json) ? json : []);
    } catch {
      setMyStats([]);
      setStatsError('Could not load stats.');
    } finally {
      setStatsLoading(false);
    }
  };

  const fetchMySummary = async () => {
    setSummaryLoading(true);
    setSummaryError(null);
    try {
      const res = await apiFetch('/api/stats/me/summary');
      if (!res.ok) {
        setMySummary(null);
        setSummaryError('Could not load summary.');
        return;
      }
      const json = await res.json();
      setMySummary(json);
    } catch {
      setMySummary(null);
      setSummaryError('Could not load summary.');
    } finally {
      setSummaryLoading(false);
    }
  };

  // Handle redirect back from OAuth flow: clear query params after first load.
  useEffect(() => {
    const url = new URL(window.location.href);
    if (url.searchParams.get('auth') === 'success') {
      // Backend set cookie; we just refresh /me and then clean URL.
      void fetchMe().finally(() => {
        url.searchParams.delete('auth');
        url.searchParams.delete('sub');
        url.searchParams.delete('email');
        url.searchParams.delete('name');
        url.searchParams.delete('picture');
        window.history.replaceState({}, document.title, url.toString());
      });
    } else {
      void fetchMe();
    }
  }, []);

  // Load stats when opening profile menu
  useEffect(() => {
    if (profileOpen && user) {
      void fetchMyStats();
      void fetchMySummary();
    }
  }, [profileOpen, user]);

  // Debug logging for profile state
  useEffect(() => {
    console.log('App: profileOpen=', profileOpen, 'user=', user ? (user.name || user.email || user.sub) : null);
  }, [profileOpen, user]);

  return (
    <main className="quiz-main">
      {/* DEBUG OVERLAY - visible only in dev builds to help debug profile/menu state */}
      {typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.DEV && (
        <div className="debug-overlay" aria-hidden>
          <div><b>Debug</b></div>
          <div>profileOpen: {String(profileOpen)}</div>
          <div>user: {user ? (user.name || user.email || user.sub) : 'null'}</div>
          <div>myStats: {myStats.length}</div>
          <div>summaryLoading: {String(summaryLoading)}</div>
        </div>
      )}

      <header className="quiz-header">
        <div className="header-row">
          <div
            className="quiz-header-link header-clickable"
            onClick={() => {
              // Quiz state lives inside the Quiz component; reload the page to reset the app to its front page.
              // This avoids referencing state setters that were moved into Quiz.
              window.location.reload();
            }}
            tabIndex={0}
            role="button"
            aria-label="Go to genre selection"
            style={{ display: 'flex', alignItems: 'center', gap: '14px', cursor: 'pointer', outline: 'none' }}
          />

          <div className="header-right">
            {!user ? (
              <GoogleLogin />
            ) : (
              <>
                <Header user={user} onProfileToggle={() => setProfileOpen(o => !o)} backendUrl={backendUrl} profileOpen={profileOpen} />
                {profileOpen && user && (
                  <ProfileMenu
                    user={user}
                    myStats={myStats}
                    mySummary={mySummary}
                    statsLoading={statsLoading}
                    statsError={statsError}
                    summaryLoading={summaryLoading}
                    onRefresh={() => { void fetchMyStats(); void fetchMySummary(); }}
                    onSignOut={async () => {
                      try {
                        await apiFetch('/api/auth/logout', { method: 'POST' });
                      } finally {
                        setUser(null);
                        setProfileOpen(false);
                      }
                    }}
                    onClose={() => setProfileOpen(false)}
                  />
                )}
              </>
            )}
          </div>
        </div>
      </header>

      {/* Left ad placeholder (fixed) */}
      <div className="ad-side left">Ad</div>

      {/* Quiz component */}
      <Quiz user={user} />

      {/* Right ad placeholder (fixed) */}
      <div className="ad-side right">Ad</div>

      {/* Mobile-bottom ad (visible only on small screens via CSS) */}
      <div className="ad-bottom">Ad</div>

      <footer className="quiz-footer">
          Powered by Deezer and Last.fm APIs. Developed by <a href="github.com/adam12240">Adam Szabo</a>
      </footer>
    </main>
  );
}
