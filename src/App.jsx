import './App.css';
import { useRef, useEffect, useState } from 'react';

const GENRES = [
  { label: 'Pop', id: 132 },
  { label: 'Rock', id: 152 },
  { label: 'Hip-Hop', id: 116 },
  { label: 'Jazz', id: 129 },
  { label: 'Classical', id: 98 },
  { label: 'Electronic', id: 106 },
  { label: 'Reggae', id: 144 },
  { label: 'Latin', id: 197 },
];

const JUMP_TIMES = [0.1, 0.5, 1, 5, 10, 30];

export default function App() {
  // Sound effects
  const correctSound = useRef(null);
  const wrongSound = useRef(null);
  const [genreId, setGenreId] = useState(GENRES[0].id);
  const [results, setResults] = useState([]);
  const [current, setCurrent] = useState(null);
  const [selected, setSelected] = useState(null);
  const [score, setScore] = useState(0);
  const [step, setStep] = useState(0);
  const [genreArtists, setGenreArtists] = useState([]);
  const [options, setOptions] = useState([]);
  const [quizEnded, setQuizEnded] = useState(false);
  const [answers, setAnswers] = useState([]);
  const [, setError] = useState(null);
  const [artistTracks, setArtistTracks] = useState({});
  const audioRef = useRef(null);
  const [jumpIndex, setJumpIndex] = useState(0);
  const [jumpTimeout] = useState(null);
  const [audioTime, setAudioTime] = useState(0);
  const [audioDuration, setAudioDuration] = useState(0);
  const [volume, setVolume] = useState(0.3);
  const [numSongs, setNumSongs] = useState(5);
  const [isPlaying, setIsPlaying] = useState(false);
  const [, setSectionStartTime] = useState(0); // When the current section started
  const [, setSectionPlayed] = useState(false); // If section already played
  const [gameMode, setGameMode] = useState('classic'); // 'classic', 'endless'
  const [streak, setStreak] = useState(0);
  const [bestStreak, setBestStreak] = useState(0);

  useEffect(() => {
    if (audioRef.current) {
      audioRef.current.volume = 0.3;
      audioRef.current.currentTime = 0;
    }
    setJumpIndex(0);
    setSectionStartTime(0);
    setSectionPlayed(false);
    if (jumpTimeout) clearTimeout(jumpTimeout);
  }, [current, jumpTimeout]);

  const searchTracks = async () => {
    setQuizEnded(false);
    setAnswers([]);
    setError(null);
    setStreak(0);
    if (gameMode === 'classic') setBestStreak(0);
    try {
      // For endless mode, fetch a large pool
      const fetchCount = gameMode === 'endless' ? 100 : numSongs;
      const res = await fetch(`http://localhost:3001/deezer?genreId=${genreId}`);
      if (!res.ok) throw new Error('Fetch failed');
      const json = await res.json();
      const filterKeywords = [
        'relaxing', 'white noise', 'therapy', 'nature', 'spa', 'sound', 'meditation', 'sleep', 'ambient', 'asmr', 'rain', 'ocean', 'thunder', 'instrumental', 'background', 'study', 'focus', 'soothing', 'calm', 'noise', 'music for', 'sound effect', 'effect', 'loop', 'baby', 'kids', 'children', 'lullaby', 'zen', 'yoga', 'hypnosis', 'guided', 'healing', 'binaural', 'frequency', '432hz', '528hz', 'delta', 'theta', 'alpha', 'beta', 'gamma', 'isochronic', 'pink noise', 'brown noise', 'fan', 'vacuum', 'hair dryer', 'washing machine', 'heartbeat', 'heartbeat sound', 'heartbeat noise', 'heartbeat effect', 'heartbeat loop', 'heartbeat music', 'heartbeat sound effect', 'heartbeat noise effect', 'heartbeat sound loop', 'heartbeat noise loop', 'heartbeat effect loop', 'heartbeat music loop', 'heartbeat sound effect loop', 'heartbeat noise effect loop', 'heartbeat sound loop loop', 'heartbeat noise loop loop', 'heartbeat effect loop loop', 'heartbeat music loop loop', 'heartbeat sound effect loop loop', 'heartbeat noise effect loop loop', 'heartbeat sound loop loop loop', 'heartbeat noise loop loop loop', 'heartbeat effect loop loop loop', 'heartbeat music loop loop loop', 'heartbeat sound effect loop loop loop', 'heartbeat noise effect loop loop loop'
      ];
      const isBadArtist = name => {
        if (!name) return true;
        if (name.length > 40) return true;
        const lower = name.toLowerCase();
        return filterKeywords.some(kw => lower.includes(kw));
      };
      const artists = (json.topArtists
        ? json.topArtists.map(a => a.name)
        : Array.from(new Set(json.data.map(track => track.artist.name))))
        .filter(name => !isBadArtist(name));
      setGenreArtists(artists);
      setArtistTracks(json.artistTracks || {});
      const shuffledTracks = json.data.sort(() => 0.5 - Math.random());
      setResults(shuffledTracks.slice(0, fetchCount));
      setCurrent(shuffledTracks[0]);
      setStep(0);
      setScore(0);
      setSelected(null);
      setJumpIndex(0);
        // eslint-disable-next-line no-unused-vars
    } catch (e) {
      setResults([]);
      setCurrent(null);
      setSelected(null);
      setStep(0);
      setScore(0);
      setQuizEnded(false);
      setAnswers([]);
      setJumpIndex(0);
    }
  };

  // Modified nextQuestion to accept the selected value as an argument
  const nextQuestion = (autoWrong = false, submittedSelected = selected) => {
    const correctArtist = (current && current.artist && current.artist.name) ? String(current.artist.name).trim().toLowerCase() : '';
    const selectedArtist = submittedSelected ? String(submittedSelected).trim().toLowerCase() : '';
    const isCorrect = autoWrong ? false : selectedArtist === correctArtist;

    // Game mode logic (classic only updates streaks here)
    if (gameMode === 'classic') {
      if (isCorrect) {
        setStreak(s => {
          const newStreak = s + 1;
          setBestStreak(b => Math.max(b, newStreak));
          return newStreak;
        });
      } else {
        setStreak(0);
      }
    }

    // Play sound effect
    if (isCorrect) {
      if (correctSound.current) {
        correctSound.current.currentTime = 0;
        correctSound.current.play();
      }
      setScore(s => s + 1);
    } else {
      if (wrongSound.current) {
        wrongSound.current.currentTime = 0;
        wrongSound.current.play();
      }
    }

    // Add this answer to the summary (include wrong answers for endless)
    setAnswers([...answers, { track: current, selected: submittedSelected, correct: current.artist.name, isCorrect }]);
    setIsPlaying(false);

    // If endless mode and the player answered incorrectly, end the quiz AFTER recording the answer
    if (gameMode === 'endless' && !isCorrect) {
      setQuizEnded(true);
      setResults([]);
      setCurrent(null);
      setJumpIndex(0);
      setGenreId(GENRES[0].id);
      return;
    }

    if (gameMode === 'endless') {
      // Pick a random next track (avoid repeats)
      const remainingTracks = results.filter(t => !answers.some(a => a.track.id === t.id) && t.id !== current.id);
      if (remainingTracks.length > 0) {
        const nextTrack = remainingTracks[Math.floor(Math.random() * remainingTracks.length)];
        setCurrent(nextTrack);
        setStep(step + 1);
        setJumpIndex(0);
        setSelected(null);
      } else {
        // If out of tracks, end the quiz
        setQuizEnded(true);
        setResults([]);
        setCurrent(null);
        setJumpIndex(0);
        setGenreId(GENRES[0].id);
      }
    } else {
      const next = step + 1;
      if (next < results.length) {
        setStep(next);
        setCurrent(results[next]);
        setJumpIndex(0);
        setSelected(null);
      } else {
        setQuizEnded(true);
        setResults([]);
        setCurrent(null);
        setJumpIndex(0);
        setGenreId(GENRES[0].id);
      }
    }
  };

  function shuffle(arr) {
    return [...arr].sort(() => 0.5 - Math.random());
  }

  useEffect(() => {
    if (!current) {
      setOptions([]);
      return;
    }
    // Always include the correct artist
    const correctArtist = current.artist.name;
    // Filter plausible fake artists by release year (±2 years)
    let plausible = genreArtists.filter(a => a !== correctArtist);
    if (current.release_date) {
      const year = parseInt(current.release_date.slice(0, 4));
      plausible = plausible.filter(a => {
        const tracks = artistTracks[a] || [];
        return tracks.some(t => {
          if (!t.release_date) return false;
          const y = parseInt(t.release_date.slice(0, 4));
          return Math.abs(y - year) <= 2;
        });
      });
    }
    // Shuffle and pick up to 3 fake artists
    const fakeArtists = shuffle(plausible).slice(0, 3);
    // Shuffle all options
    setOptions(shuffle([correctArtist, ...fakeArtists]));
  }, [current, genreArtists, artistTracks]);

  return (
    <main className="quiz-main">
      {/* Sound effects */}
      <audio ref={correctSound} src="/correct.wav" preload="auto" />
      <audio ref={wrongSound} src="/wrong.wav" preload="auto" />
      <header className="quiz-header">
        <div
          className="quiz-header-link header-clickable"
          onClick={() => {
            setResults([]);
            setCurrent(null);
            setSelected(null);
            setStep(0);
            setScore(0);
            setQuizEnded(false);
            setJumpIndex(0);
            setAnswers([]);
          }}
          tabIndex={0}
          role="button"
          aria-label="Go to genre selection"
          style={{ display: 'flex', alignItems: 'center', gap: '14px', cursor: 'pointer', outline: 'none' }}
        >
        </div>
      </header>
      <div className="quiz-card">
         {/* Album Art Background with Dimming */}
         {current && current.album && (current.album.cover_xl || current.album.cover_big) && (
          <img
            src={current.album.cover_xl || current.album.cover_big}
            alt="Album Art"
            className="album-bg"
            style={{ filter: `brightness(${0.08 + 0.7 * (jumpIndex / (JUMP_TIMES.length - 1))})` }}
          />
         )}
         {/* Overlay for readability */}
         {current && (
          <div className="album-overlay" />
         )}
         {/* Animated Progress Bar */}
         {current && (
          <div className="top-progress">
            <div className="top-progress-fill" style={{ width: `${((step + (isPlaying ? audioTime / (current.preview ? 30 : 1) : 0)) / results.length) * 100}%` }} />
          </div>
         )}
         {/* Foreground content below */}
        <div className="foreground">
        {/* Foreground content actual */}
        <div className="foreground-inner">
           {current && (
            <div className="title-row">
              <span
                className="quiz-title quiz-header-link quiz-title-link"
                onClick={() => {
                  setResults([]);
                  setCurrent(null);
                  setSelected(null);
                  setStep(0);
                  setScore(0);
                  setQuizEnded(false);
                  setJumpIndex(0);
                  setAnswers([]);
                }}
                tabIndex={0}
                role="button"
                aria-label="Go to genre selection"
              >
                <img src="/headphones.png" alt="TuneTrivia Logo" className="quiz-logo" />TuneTrivia
              </span>
            </div>
           )}
           {current && (
            <div className="top-row">
              <div className="score-streak">{gameMode === 'classic' ? `Streak: ${streak} | Best: ${bestStreak}` : `Score: ${score}`}</div>
              <h2 className="quiz-step">Question {step + 1} / {gameMode === 'endless' ? '∞' : results.length}</h2>
            </div>
             
           )}
          {!current && !quizEnded && (
            <div className="quiz-search">
               <div style={{ marginBottom: '18px', textAlign: 'center' }}>
                <img src="/headphones.png" alt="TuneTrivia Logo" className="quiz-logo quiz-logo-large" />
                <h1 className="quiz-main-title">TuneTrivia</h1>
                <div className="quiz-subtitle">Guess the artist from the music preview!</div>
               </div>
              <div className="quiz-search-controls">
                <label className="quiz-search-label">Genre
                  <select className="quiz-input" value={genreId} onChange={e => setGenreId(Number(e.target.value))}>
                    {GENRES.map(g => (
                      <option key={g.id} value={g.id}>{g.label}</option>
                    ))}
                  </select>
                </label>
                {gameMode !== 'endless' && (
                  <label className="quiz-search-label">
                    Number of songs<br />
                    <input
                      type="number"
                      min={1}
                      max={20}
                      value={numSongs}
                      onChange={e => setNumSongs(Math.max(1, Math.min(20, Number(e.target.value))))}
                      className="quiz-input quiz-number-input"
                      aria-label="Number of songs"
                    />
                  </label>
                )}
                <label className="quiz-search-label">Game Mode
                  <select className="quiz-input" value={gameMode} onChange={e => setGameMode(e.target.value)}>
                    <option value="classic">Classic</option>
                    <option value="endless">Endless</option>
                  </select>
                </label>
                <button className="quiz-btn quiz-start-btn" onClick={() => searchTracks(numSongs)}>Start Quiz</button>
              </div>
             </div>
           )}
          {current && (
            <div className="quiz-question">
              {/* Question number and score are now above, removed from here */}
              <div className="player-row">
                <audio
                  ref={audioRef}
                  src={current.preview}
                  className="hidden-audio"
                  onTimeUpdate={() => {
                    setAudioTime(audioRef.current.currentTime);
                    // Only play from 0s to the current section end
                    if (audioRef.current && jumpIndex < JUMP_TIMES.length) {
                      const sectionEnd = JUMP_TIMES[jumpIndex];
                      if (audioRef.current.currentTime >= sectionEnd) {
                        audioRef.current.pause();
                        setSectionPlayed(true);
                      }
                    }
                  }}
                  onLoadedMetadata={() => {
                    setAudioDuration(audioRef.current.duration);
                    audioRef.current.volume = volume;
                  }}
                  onPlay={() => {
                    setIsPlaying(true);
                    setSectionStartTime(0);
                  }}
                  onPause={() => setIsPlaying(false)}
                />
                <div className="player-row-inner">
                  {/* Volume Bar Left */}
                  <div className="player-volume">
                    <span className="player-label">Volume</span>
                    <input
                      type="range"
                      min={0}
                      max={1}
                      step={0.01}
                      value={volume}
                      onChange={e => {
                        setVolume(Number(e.target.value));
                        if (audioRef.current) audioRef.current.volume = Number(e.target.value);
                      }}
                      className="player-volume-bar"
                      aria-label="Volume"
                    />
                  </div>
                  {/* Play Button Center */}
                  <div className="player-center">
                    <button
                      className="jump-btn"
                      onClick={() => {
                        // Only skip, do not play sound
                        if (audioRef.current && jumpIndex < JUMP_TIMES.length - 1) {
                          // Remove any previous timeupdate listeners
                          if (audioRef.current._jumpHandler) {
                            audioRef.current.removeEventListener('timeupdate', audioRef.current._jumpHandler);
                            audioRef.current._jumpHandler = null;
                          }
                          setJumpIndex(jumpIndex + 1);
                          audioRef.current.currentTime = 0;
                          audioRef.current.pause();
                          setSectionPlayed(false); // Allow replaying this section
                          setSectionStartTime(0);
                        }
                      }}
                      disabled={jumpIndex >= JUMP_TIMES.length - 1}
                    >
                      {jumpIndex + 1 === JUMP_TIMES.length
                        ? 'Skip to end of track'
                        : `Skip to ${JUMP_TIMES[jumpIndex + 1]} seconds`}
                    </button>
                    <div style={{ display: 'flex', flexDirection: 'row', gap: '8px', marginTop: '12px' }}>
                      <button
                        className="play-btn"
                        onClick={() => {
                          // Always allow replay: reset to 0 and play section again
                          if (
                            audioRef.current &&
                            !isPlaying &&
                            audioRef.current.paused
                          ) {
                            // Remove any previous timeupdate listeners
                            if (audioRef.current._jumpHandler) {
                              audioRef.current.removeEventListener('timeupdate', audioRef.current._jumpHandler);
                              audioRef.current._jumpHandler = null;
                            }
                            audioRef.current.currentTime = 0;
                            setSectionPlayed(false); // Allow replaying
                            audioRef.current.play();
                          }
                        }}
                        disabled={
                          !audioRef.current || isPlaying || (audioRef.current && !audioRef.current.paused)
                        }
                      >
                        Play
                      </button>
                      <button
                        className="stop-btn"
                        onClick={() => {
                          if (audioRef.current && isPlaying) {
                            // Remove any previous timeupdate listeners
                            if (audioRef.current._jumpHandler) {
                              audioRef.current.removeEventListener('timeupdate', audioRef.current._jumpHandler);
                              audioRef.current._jumpHandler = null;
                            }
                            audioRef.current.pause();
                          }
                        }}
                        disabled={!audioRef.current || !isPlaying}
                      >
                        Stop
                      </button>
                    </div>
                  </div>
                  {/* Music Length Bar Right Vertical, fills from top */}
                  <div className="player-progress">
                    <span className="player-label">Progress</span>
                    <div className="player-progress-bar">
                      <div
                        className="player-progress-fill"
                        style={{
                          height: audioDuration ? `${(audioTime / audioDuration) * 100}%` : '0%',
                        }}
                      />
                    </div>
                  </div>
                </div>
              </div>
              <p className="quiz-label">Who is the artist?</p>
              <ul className="quiz-list">
                {options.map((artist, idx) => (
                  <li key={idx} className="quiz-list-item">
                    <label className="quiz-radio-label">
                      <input
                        type="radio"
                        name="answer"
                        value={artist}
                        checked={selected === artist}
                        onChange={() => setSelected(artist)}
                        className="quiz-radio"
                      />
                      {artist}
                    </label>
                  </li>
                ))}
              </ul>
              <button className="quiz-btn" onClick={() => nextQuestion(false, selected)} disabled={!selected}>
                Submit
              </button>
            </div>
          )}
          {quizEnded && (
            <div className="results-page">
              <header className="quiz-header">
                <span
                  className="quiz-title"
                  onClick={() => {
                    setResults([]);
                    setCurrent(null);
                    setSelected(null);
                    setStep(0);
                    setScore(0);
                    setQuizEnded(false);
                    setAnswers([]);
                    setJumpIndex(0);
                  }}
                  tabIndex={0}
                  role="button"
                  aria-label="Go to genre selection"
                >
                </span>
              </header>
              <span
                className="quiz-title quiz-header-link"
                onClick={() => {
                  setResults([]);
                  setCurrent(null);
                  setSelected(null);
                  setStep(0);
                  setScore(0);
                  setQuizEnded(false);
                  setJumpIndex(0);
                  setAnswers([]);
                }}
                tabIndex={0}
                role="button"
                aria-label="Go to genre selection"
                style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 10 }}
              >
                <img src="/headphones.png" alt="TuneTrivia Logo" className="quiz-logo" />TuneTrivia
              </span>
              <br></br>
              <h2 className="results-title">
                Quiz Results
              </h2>
              <div className="results-score">Your score: {score} / {answers.length}</div>
              <div className="results-list-container">
                <ul className="results-list">
                  {answers.map((ans, idx) => (
                    <li key={idx} className={`results-item ${ans.isCorrect ? 'correct' : 'incorrect'}`}>
                      <div className="results-track">
                        <span className="results-q">Q{idx + 1}:</span>
                        <audio src={ans.track.preview} controls className="results-audio" onLoadedMetadata={e => e.target.volume = 0.3} />
                      </div>
                      <div className="results-answer">
                        <span>Correct: <b>{ans.correct} - {ans.track.title}</b></span>
                        <span>Your answer: <b className={ans.isCorrect ? 'correct' : 'incorrect'}>{ans.selected}</b></span>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
              <button className="quiz-btn" onClick={() => {
                setResults([]);
                setCurrent(null);
                setSelected(null);
                setStep(0);
                setScore(0);
                setQuizEnded(false);
                setJumpIndex(0);
                setAnswers([]);
              }}>
                Play Again
              </button>
            </div>
          )}
        </div>
        {/* End Foreground content actual */}
      </div>
      </div>
      <footer className="quiz-footer">
        Powered by Deezer
      </footer>
    </main>
  );
}