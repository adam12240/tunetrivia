import express from 'express';
import fetch from 'node-fetch';
import cors from 'cors';

const app = express();
const PORT = 3001;

app.use(cors());

app.get('/deezer', async (req, res) => {
  const { genreId, q, genre } = req.query;
  let apiURL;

  if (genreId) {
    try {
      // Fetch top 100 tracks for the genre
      const chartRes = await fetch(`https://api.deezer.com/chart/${genreId}/tracks?limit=300`);
      const chartData = await chartRes.json();
      const topTracks = chartData.data || [];

        // eslint-disable-next-line no-control-regex
      const nonAscii = /[^\x00-\x7F]/;
      const filteredTracks = topTracks.filter(
        track => !nonAscii.test(track.title) && !nonAscii.test(track.artist.name)
      );

      // Shuffle and pick 5 random tracks
      const shuffledTracks = filteredTracks.sort(() => 0.5 - Math.random());
      const quizTracks = shuffledTracks.slice(0, 20);

      // Collect unique artists from these tracks
      const topArtists = Array.from(
        new Set(filteredTracks.map(track => track.artist.name))
      ).map(name => filteredTracks.find(track => track.artist.name === name).artist);

      // Build artistTracks: { [artistName]: [{ title, release_date }] }
      const artistTracks = {};
      filteredTracks.forEach(track => {
        if (!artistTracks[track.artist.name]) artistTracks[track.artist.name] = [];
        artistTracks[track.artist.name].push({ title: track.title, release_date: track.release_date });
      });

      res.json({ data: quizTracks, topArtists, artistTracks });
      return;
    } catch (err) {
      console.error('Error fetching genre tracks from Deezer:', err);
      res.status(500).json({ error: 'Failed to fetch genre tracks from Deezer API' });
      return;
    }
  }

  // Fallback to search by query or genre keyword
  const searchQuery = genre || q;
  if (!searchQuery) return res.status(400).json({ error: 'Missing search query (?q=...) or genre (?genre=...) or genreId (?genreId=...)' });
  apiURL = `https://api.deezer.com/search?q=${encodeURIComponent(searchQuery)}`;

  try {
    const response = await fetch(apiURL);
    const data = await response.json();
    res.json(data);
  } catch (err) {
    console.error('Error fetching from Deezer:', err);
    res.status(500).json({ error: 'Failed to fetch from Deezer API' });
  }
});

app.listen(PORT, () => {
  console.log(`Proxy running at http://localhost:${PORT}/deezer?q=SEARCH_TERM`);
});