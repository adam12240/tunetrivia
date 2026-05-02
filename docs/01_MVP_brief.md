# MVP — TuneTrivia (megvalósítva)

## 1. Rövid összefoglaló
A TuneTrivia MVP célja rövid, zenei felismerési kvízek biztosítása webes felületen. Az MVP megvalósítása megtörtént: a frontend React+Vite alapú, a backend Spring Boot, az adatbázis PostgreSQL, és a legfontosabb felhasználói utak (Google bejelentkezés, egyjátékos kvíz, eredménymentés) működnek.

## 2. Megvalósított MVP elemek és elfogadási kritériumok
| Elem | Állapot | Indoklás / elfogadási kritérium                                                                                                                                |
|---|---:|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Google OAuth belépés | Megvalósítva | POST `/api/auth/google` (idToken ellenőrzés), GET `/api/auth/me` elérhető; a frontend képes bejelentkeztetni a felhasználót és profiladatokat mutatni.         |
| Egyjátékos kvíz| Megvalósítva | A frontend indítja a kvízt, x kérdés betöltődik (Deezer/Last.fm proxy vagy szerveren tárolt kérdéskészlet); a játék lefut, válaszadás és újrakérdezés működik. |
| Eredménymentés / PlayStat | Megvalósítva | POST `/api/stats` menti a játék eredményét; GET `/api/stats/me` lekéri a felhasználó statisztikáit.                                                            |
| Avatar proxy | Megvalósítva | GET `/api/auth/avatar?u=...` proxy az engedett hostokról; content-type ellenőrzés és alap szűrés.                                                              |
| Deezer / Last.fm integráció (proxy/import) | Megvalósítva | `/deezer` és `/api/lastfm/*` végpontok léteznek és használhatók import/keresés céljára.                                                                        |

## 3. Nem célok (szándékosan kihagyottak az MVP-ből)
- Teljes Spotify streaming integráció
- Komplex ajánlórendszer és személyre szabott feed.
- Offline-first natív mobil alkalmazás — a web responsive felület az MVP hangsúlya.

## 4. Sikerességi mérőszámok és aktuális állapot
| Mérőszám | Cél | Aktuális állapot |
|---|---|---|
| Google OAuth integráció | 99% | Sikeres: idToken validáció backendben, profil tükröződik frontendben. |
| Telepíthetőség reprodukálhatósága | dokumentált | `docker-compose up --build` indítja a backend+db-et; frontend külön indítható (`npm run dev`). |

## 5. Ellenőrzés / gyors reprodukció (lokálisan)
A projekt gyökere alatt a gyors indításhoz használható parancsok (példák):

```bash
# Docker-alapú gyors indítás a projekt gyökérből
docker-compose up --build

# Backend alternatív indítása lokálisan (ha nem Docker-rel)
cd backend
mvn spring-boot:run

# Frontend indítása fejlesztői módban
cd frontend
npm install
npm run dev
```

A fenti lépésekkel a backend elérhető lesz `http://localhost:3002`, a frontend alapértelmezett portja `http://localhost:5173`.

## 6. Rövid technológiai áttekintés
- Backend: Spring Boot (Java), Flyway migrációk a `backend/src/main/resources/db/migration` alatt.
- Adatbázis: PostgreSQL (Docker Compose konfiguráció megtalálható a projekt gyökérben).
- Frontend: React + Vite.
- Integrációk: Deezer proxy, Last.fm import; Google OAuth idToken validálás.

