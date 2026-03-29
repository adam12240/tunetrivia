import React from 'react';
import { createPortal } from 'react-dom';

function ProfileMenuContent({ user, myStats, mySummary, statsLoading, statsError, summaryLoading, onRefresh, onSignOut, onClose }) {
  return (
    <>
      <div className="profile-overlay" onClick={onClose} />
      <div className="profile-menu profile-menu-center" role="menu">
        <div className="profile-menu-head">
          <div className="profile-menu-title">Signed in</div>
          <div className="profile-menu-sub">{user.email || user.sub}</div>
        </div>

        <div className="profile-menu-section">
          <div className="profile-menu-section-title">Your statistics</div>
          {statsLoading && <div className="profile-menu-muted">Loading…</div>}
          {statsError && <div className="profile-menu-error">{statsError}</div>}
          {!statsLoading && !statsError && (
            <>
              <div className="stats-grid">
                <div className="stat-card">
                  <div className="stat-label">Songs logged</div>
                  <div className="stat-value">{myStats.length}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Correct</div>
                  <div className="stat-value">{myStats.filter(s => s.correct).length}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Accuracy</div>
                  <div className="stat-value">{myStats.length ? `${Math.round((myStats.filter(s => s.correct).length / myStats.length) * 100)}%` : '—'}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Avg skips / song</div>
                  <div className="stat-value">{summaryLoading ? '…' : mySummary ? (Number.isFinite(mySummary.averageSkipsPerSong) ? mySummary.averageSkipsPerSong.toFixed(1) : '—') : '—'}</div>
                </div>
              </div>
              <button className="profile-mini-btn" onClick={onRefresh}>Refresh</button>
            </>
          )}
        </div>

        <div className="profile-menu-actions">
          <button className="profile-mini-btn danger" onClick={onSignOut}>Sign out</button>
        </div>
      </div>
    </>
  );
}

export default function ProfileMenu(props) {
  if (typeof document === 'undefined') return null;
  return createPortal(
    <ProfileMenuContent {...props} />,
    document.body
  );
}
