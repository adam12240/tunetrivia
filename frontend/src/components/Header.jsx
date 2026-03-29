import React from 'react';
import GoogleLogin from './GoogleLogin.jsx';

export default function Header({ user, onProfileToggle, backendUrl}) {
  return (
    <div className="header-right">
      {!user ? (
        <GoogleLogin />
      ) : (
        <div className="profile-wrap">
          <button
            className="profile-btn"
            onClick={() => { console.log('Header: profile button clicked'); onProfileToggle(); }}
            aria-label="Open profile menu"
          >
            <img
              className="profile-avatar"
              src={user.picture ? `${backendUrl}/api/auth/avatar?u=${encodeURIComponent(user.picture)}` : '/headphones.png'}
              alt="Profile"
            />
            <span className="profile-name">{user.name || user.email || 'Profile'}</span>
          </button>
        </div>
      )}
    </div>
  );
}
