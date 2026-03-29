export default function GoogleLogin() {
  // backend base url (use Vite env if provided)
  const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:3002';

  return (
    <div style={{ textAlign: 'center', marginBottom: 16 }}>
      <button
        className="quiz-btn"
        onClick={() => {
          // start server-side OAuth flow on backend (optional)
          window.location.href = `${backendUrl}/api/auth/start`;
        }}
      >
        Sign in with Google (optional)
      </button>
    </div>
  );
}
