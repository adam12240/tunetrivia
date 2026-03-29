import React, { useRef, useEffect, useState } from 'react';
import { apiFetch } from '../services/api';
import Results from './Results.jsx';

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

// Helper: remove bracketed content like (feat ...) [live] {remix} from displayable titles
const stripBrackets = (s) => {
  if (!s || typeof s !== 'string') return '';
  return s.replace(/\s*[([{][^)\]}]*[)\]}]\s*/g, ' ').replace(/\s+/g, ' ').trim();
};

export default function Quiz({ user }) {
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
  // Keep sound effect volumes in sync with the player volume (declared after volume state)
  useEffect(() => {
    if (correctSound.current) correctSound.current.volume = volume;
    if (wrongSound.current) wrongSound.current.volume = volume;
  }, [volume]);
  const [numSongs, setNumSongs] = useState(5);
  const [isPlaying, setIsPlaying] = useState(false);
  const [, setSectionStartTime] = useState(0);
  const [, setSectionPlayed] = useState(false);
  const [gameMode, setGameMode] = useState('classic');
  const [, setStreak] = useState(0);
  const [, setBestStreak] = useState(0);
  const [currentSkips, setCurrentSkips] = useState(0);

  const genreLabelById = GENRES.reduce((acc, g) => {
    acc[g.id] = g.label;
    return acc;
  }, {});

  const savePlayStat = async ({ track, correctArtist, isCorrect }) => {
    try {
      if (!track) return;
      await apiFetch('/api/stats', {
        method: 'POST',
        body: JSON.stringify({
          genre: genreLabelById[genreId] || String(genreId),
          trackId: String(track.id ?? ''),
          artist: String(correctArtist ?? ''),
          title: String(track.title ?? ''),
          correct: Boolean(isCorrect),
          skips: currentSkips ?? null,
        }),
      });
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    if (audioRef.current) {
      audioRef.current.volume = 0.3;
      audioRef.current.currentTime = 0;
    }
    setJumpIndex(0);
    setSectionStartTime(0);
    setSectionPlayed(false);
    setCurrentSkips(0);
    if (jumpTimeout) clearTimeout(jumpTimeout);
  }, [current, jumpTimeout]);

  const searchTracks = async () => {
    setQuizEnded(false);
    setAnswers([]);
    setError(null);
    setStreak(0);
    if (gameMode === 'classic') setBestStreak(0);
    try {
      const fetchCount = gameMode === 'endless' ? 100 : numSongs;
      const res = await apiFetch(`/deezer?genreId=${genreId}`);
      if (!res.ok) return;
      const json = await res.json();
      const filterKeywords = [
        'relaxing','white noise','therapy','nature','spa','sound','meditation','sleep','ambient','asmr','rain','ocean','thunder','instrumental','background','study','focus','soothing','calm','noise'
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

  const fetchMoreEndlessTracks = async (alreadyAnsweredIds) => {
    try {
      const res = await apiFetch(`/deezer?genreId=${genreId}`);
      if (!res.ok) return [];
      const json = await res.json();
      const incoming = Array.isArray(json.data) ? json.data : [];
      const filtered = incoming.filter(t => t && t.id && !alreadyAnsweredIds.has(t.id));
      return filtered.sort(() => 0.5 - Math.random());
    } catch {
      return [];
    }
  };

  const nextQuestion = async (autoWrong = false, submittedSelected = selected) => {
    const mode = (step % 2 === 0) ? 'artist' : 'title';
    const correctValue = mode === 'artist'
      ? ((current && current.artist && current.artist.name) ? String(current.artist.name).trim().toLowerCase() : '')
      : ((current && current.title) ? String(stripBrackets(current.title)).trim().toLowerCase() : '');
    const selectedValue = submittedSelected ? String(submittedSelected).trim().toLowerCase() : '';
    const isCorrect = autoWrong ? false : selectedValue === correctValue;

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

    if (isCorrect) {
      if (correctSound.current) {
        try {
          console.debug('Attempting to play correct sound', correctSound.current && correctSound.current.src, 'readyState=', correctSound.current && correctSound.current.readyState);
          // Ensure the audio is loaded; some environments require load() before play()
            // eslint-disable-next-line no-unused-vars
          try { correctSound.current.load(); } catch (e) { /* ignore */ }
          correctSound.current.currentTime = 0;
          const p = correctSound.current.play();
          if (p && typeof p.then === 'function') p.catch(err => console.debug('correct sound play prevented:', err));
        } catch (err) {
          console.debug('correct sound play failed sync:', err);
        }
      }
      setScore(s => s + 1);
    } else {
      if (wrongSound.current) {
        try {
          console.debug('Attempting to play wrong sound', wrongSound.current && wrongSound.current.src, 'readyState=', wrongSound.current && wrongSound.current.readyState);
            // eslint-disable-next-line no-unused-vars
          try { wrongSound.current.load(); } catch (e) { /* ignore */ }
          wrongSound.current.currentTime = 0;
          const p = wrongSound.current.play();
          if (p && typeof p.then === 'function') p.catch(err => console.debug('wrong sound play prevented:', err));
        } catch (err) {
          console.debug('wrong sound play failed sync:', err);
        }
      }
    }

    const nextAnswers = [...answers, { track: current, selected: submittedSelected, correct: current.artist.name, isCorrect }];
    setAnswers(nextAnswers);
    setIsPlaying(false);

    if (user) {
      await savePlayStat({ track: current, correctArtist: current?.artist?.name, isCorrect });
      setCurrentSkips(0);
    }

    if (gameMode === 'endless' && !isCorrect) {
      setQuizEnded(true);
      setResults([]);
      setCurrent(null);
      setJumpIndex(0);
      setGenreId(GENRES[0].id);
      return;
    }

    if (gameMode === 'endless') {
      const answeredIds = new Set(nextAnswers.map(a => a?.track?.id).filter(Boolean));
      let nextPool = results.filter(t => t && t.id && !answeredIds.has(t.id) && t.id !== current.id);

      if (nextPool.length < 10) {
        const more = await fetchMoreEndlessTracks(answeredIds);
        const existingIds = new Set(results.map(t => t?.id).filter(Boolean));
        const merged = [...results, ...more.filter(t => t?.id && !existingIds.has(t.id))];
        setResults(merged);
        nextPool = merged.filter(t => t && t.id && !answeredIds.has(t.id) && t.id !== current.id);
      }

      if (nextPool.length > 0) {
        const nextTrack = nextPool[Math.floor(Math.random() * nextPool.length)];
        setCurrent(nextTrack);
        setStep(s => s + 1);
        setJumpIndex(0);
        setSelected(null);
      } else {
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

  function shuffle(arr) { return [...arr].sort(() => 0.5 - Math.random()); }

  useEffect(() => {
    if (!current) {
      setOptions([]);
      return;
    }
    const mode = (step % 2 === 0) ? 'artist' : 'title';
    if (mode === 'artist') {
      const correctArtist = current.artist.name;
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
      const fakeArtists = shuffle(plausible).slice(0, 3);
      setOptions(shuffle([correctArtist, ...fakeArtists]));
    } else {
      const correctTitle = current.title || '';
      const correctTitleStripped = stripBrackets(correctTitle);
      const candidates = new Set();
      for (const t of results) { if (t && t.title && t.title !== correctTitle) candidates.add(t.title); }
      Object.values(artistTracks || {}).forEach(list => { (list || []).forEach(t => { if (t && t.title && t.title !== correctTitle) candidates.add(t.title); }); });
      const candidateArr = Array.from(candidates).filter(Boolean);
      const fakeTitles = shuffle(candidateArr).slice(0, 3).map(t => stripBrackets(t));
      setOptions(shuffle([correctTitleStripped, ...fakeTitles]));
    }
  }, [current, genreArtists, artistTracks, results, step]);

  return (
    <div className="quiz-card">
      {/* sound effects (short clips) */}
      <audio ref={correctSound} src="/correct.wav" preload="auto" />
      <audio ref={wrongSound} src="/wrong.wav" preload="auto" />

      {current && current.album && (current.album.cover_xl || current.album.cover_big) && (
        <img src={current.album.cover_xl || current.album.cover_big} alt="Album Art" className="album-bg" style={{ filter: `brightness(${0.08 + 0.7 * (jumpIndex / (JUMP_TIMES.length - 1))})` }} />
      )}
      {current && <div className="album-overlay" />}

      {current && gameMode !== 'endless' && (
        <div className="top-progress">
          <div className="top-progress-fill" style={{ width: `${((step + (isPlaying ? audioTime / (current.preview ? 30 : 1) : 0)) / results.length) * 100}%` }} />
        </div>
      )}

      <div className="foreground">
        <div className="foreground-inner">
          {current && (
            <div className="title-row"><span className="quiz-title quiz-header-link quiz-title-link" tabIndex={0} role="button" aria-label="Go to genre selection"><img src="/headphones.png" alt="TuneTrivia Logo" className="quiz-logo" />TuneTrivia</span></div>
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
                  <select className="quiz-input" value={genreId} onChange={e => setGenreId(Number(e.target.value))}>{GENRES.map(g => (<option key={g.id} value={g.id}>{g.label}</option>))}</select>
                </label>
                {gameMode !== 'endless' && (
                  <label className="quiz-search-label">Number of songs<br /><input type="number" min={1} max={20} value={numSongs} onChange={e => setNumSongs(Math.max(1, Math.min(20, Number(e.target.value))))} className="quiz-input quiz-number-input" aria-label="Number of songs" /></label>
                )}
                <label className="quiz-search-label">Game Mode
                  <select className="quiz-input" value={gameMode} onChange={e => setGameMode(e.target.value)}><option value="classic">Classic</option><option value="endless">Endless</option></select>
                </label>
                <button className="quiz-btn quiz-start-btn" onClick={() => searchTracks(numSongs)}>Start Quiz</button>
              </div>
            </div>
          )}

          {current && (
            <div className="quiz-question">
              <div className="player-row">
                <audio ref={audioRef} src={current.preview} className="hidden-audio" onTimeUpdate={() => { setAudioTime(audioRef.current.currentTime); if (audioRef.current && jumpIndex < JUMP_TIMES.length) { const sectionEnd = JUMP_TIMES[jumpIndex]; if (audioRef.current.currentTime >= sectionEnd) { audioRef.current.pause(); setSectionPlayed(true); } } }} onLoadedMetadata={() => { setAudioDuration(audioRef.current.duration); audioRef.current.volume = volume; }} onPlay={() => { setIsPlaying(true); setSectionStartTime(0); }} onPause={() => setIsPlaying(false)} />
                <div className="player-row-inner">
                  <div className="player-volume"><span className="player-label">Volume</span><input type="range" min={0} max={1} step={0.01} value={volume} onChange={e => { setVolume(Number(e.target.value)); if (audioRef.current) audioRef.current.volume = Number(e.target.value); }} className="player-volume-bar" aria-label="Volume" /></div>
                  <div className="player-center">
                    <button className="jump-btn" onClick={() => { setCurrentSkips(s => s + 1); if (audioRef.current && jumpIndex < JUMP_TIMES.length - 1) { if (audioRef.current._jumpHandler) { audioRef.current.removeEventListener('timeupdate', audioRef.current._jumpHandler); audioRef.current._jumpHandler = null; } setJumpIndex(jumpIndex + 1); audioRef.current.currentTime = 0; audioRef.current.pause(); setSectionPlayed(false); setSectionStartTime(0); } }} disabled={jumpIndex >= JUMP_TIMES.length - 1}>{jumpIndex + 1 === JUMP_TIMES.length ? 'Skip to end of track' : `Skip to ${JUMP_TIMES[jumpIndex + 1]} seconds`}</button>
                    <div style={{ display: 'flex', flexDirection: 'row', gap: '8px', marginTop: '12px' }}>
                      <button className="play-btn" onClick={() => { if (audioRef.current && !isPlaying && audioRef.current.paused) { if (audioRef.current._jumpHandler) { audioRef.current.removeEventListener('timeupdate', audioRef.current._jumpHandler); audioRef.current._jumpHandler = null; } audioRef.current.currentTime = 0; setSectionPlayed(false); audioRef.current.play(); } }} disabled={!audioRef.current || isPlaying || (audioRef.current && !audioRef.current.paused)}>Play</button>
                      <button className="stop-btn" onClick={() => { if (audioRef.current && isPlaying) { if (audioRef.current._jumpHandler) { audioRef.current.removeEventListener('timeupdate', audioRef.current._jumpHandler); audioRef.current._jumpHandler = null; } audioRef.current.pause(); } }} disabled={!audioRef.current || !isPlaying}>Stop</button>
                    </div>
                  </div>
                  <div className="player-progress"><span className="player-label">Progress</span><div className="player-progress-bar"><div className="player-progress-fill" style={{ height: audioDuration ? `${(audioTime / audioDuration) * 100}%` : '0%' }} /></div></div>
                </div>
              </div>
              <p className="quiz-label">{(step % 2 === 0)
                ? <>Who is the <strong>artist?</strong></>
                : <>Which is the <strong>song title?</strong></>
              }</p>
              <ul className="quiz-list">{options.map((artist, idx) => (<li key={idx} className="quiz-list-item"><label className="quiz-radio-label"><input type="radio" name="answer" value={artist} checked={selected === artist} onChange={() => setSelected(artist)} className="quiz-radio" />{artist}</label></li>))}</ul>
              <button className="quiz-btn" onClick={() => nextQuestion(false, selected)} disabled={!selected}>Submit</button>
            </div>
          )}

          {quizEnded && (
            <Results
              answers={answers}
              score={score}
              onPlayAgain={() => {
                setResults([]);
                setCurrent(null);
                setSelected(null);
                setStep(0);
                setScore(0);
                setQuizEnded(false);
                setJumpIndex(0);
                setAnswers([]);
                setCurrentSkips(0);
              }}
            />
          )}
        </div>
      </div>
    </div>
  );
}
