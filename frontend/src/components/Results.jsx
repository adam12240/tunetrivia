import React from 'react';

// Small helper to strip bracketed parts from titles (same behavior as in Quiz)
const stripBrackets = (s) => {
  if (!s || typeof s !== 'string') return '';
  return s.replace(/\s*[([{][^)\]}]*[)\]}]\s*/g, ' ').replace(/\s+/g, ' ').trim();
};

export default function Results({ answers = [], score = 0, onPlayAgain = () => {} }) {
  return (
    <div className="results-page">
      <header className="quiz-header">
        <span className="quiz-title" tabIndex={0} role="button" aria-label="Go to genre selection" />
      </header>

      <span className="quiz-title quiz-header-link" tabIndex={0} role="button" aria-label="Go to genre selection" style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 10 }}>
        <img src="/headphones.png" alt="TuneTrivia Logo" className="quiz-logo" />TuneTrivia
      </span>

      <br />
      <h2 className="results-title">Quiz Results</h2>
      <div className="results-score">Your score: {score} / {answers.length}</div>

      <div className="results-list-container">
        <ul className="results-list">
          {answers.map((ans, idx) => (
            <li key={idx} className={`results-item ${ans.isCorrect ? 'correct' : 'incorrect'}`}>
              <div className="results-track">
                <span className="results-q">Q{idx + 1}:</span>
                <audio src={ans.track?.preview} controls className="results-audio" onLoadedMetadata={e => { try { e.target.volume = 0.3; } catch {} }} />
              </div>
              <div className="results-answer">
                <span>Correct: <b>{ans.correct} - {stripBrackets(ans.track?.title)}</b></span>
                <span>Your answer: <b className={ans.isCorrect ? 'correct' : 'incorrect'}>{ans.selected}</b></span>
              </div>
            </li>
          ))}
        </ul>
      </div>

      <button className="quiz-btn" onClick={onPlayAgain}>Play Again</button>
    </div>
  );
}

