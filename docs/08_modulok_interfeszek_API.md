# Modulok, interfészek és API — TuneTrivia

## Fő modulok (rokonság a backend kóddal)
- `AuthService` / `AuthController` — Google id token ellenőrzés, cookie alapú session kezelés, avatar proxy.
- `StatsService` / `StatsController` — `PlayStat` mentés és lekérés (`/api/stats`).
- `LastFmService` / `LastFmController` — Last.fm import és top-tracks export (`/api/lastfm/*`).
- `DeezerProxyService` / `DeezerProxyController` — Deezer proxy és keresés (`/deezer`).

## Implementált API végpontok (aktuális állapot)
- POST `/api/auth/google` — request: { idToken } → response: { token, email, name }
- GET `/api/auth/me` — jelenlegi bejelentkezett user (ha van)
- POST `/api/auth/logout` — session cookie törlése
- GET `/api/auth/avatar?u=<url>` — korlátozott avatar proxy (csak engedett hostok)
- GET `/api/auth/start` — (OAuth redirect start) átirányítás Google-ra (ha clientId konfigurálva)
- GET `/api/auth/callback` — OAuth callback delegálása az AuthService-nek
- POST `/api/stats` — PlayStat mentése
- GET `/api/stats/me` — felhasználó saját statisztikái
- GET `/api/stats/me/summary` — statisztikai összegzés
- GET `/api/lastfm/top-tracks?user=...` — Last.fm top tracks import
- GET `/api/lastfm/playlist?url=...` — Last.fm playlist import
- GET `/deezer` — Deezer proxy/search endpoint


- PlayStat: { id, userId, quizId, score, correctCount, durationMs, timestamp }

## Tesztelési megjegyzés
A dokumentáció tükrözi a jelenlegi kódimplementációt; ha endpointokat mozgatunk vagy átnevezünk, frissíteni kell a fenti listát.
