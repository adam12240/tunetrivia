# TuneTrivia

Egyszerű, Vite + React + Java Spring alapú zenei kvíz alkalmazás Deezer és Last.fm API-val.

Funkciók

- Különböző játékmódok: rövid (klasszikus) játék és végtelen módban történő folyamatos játék.
- Műfaj szerinti válogatás (a frontend a backenden keresztül tölti be a Deezer top trackjeit egy adott műfajból).
- Véletlenszerű kérdések a lekért számokból (a backend szűri a nem ASCII karaktereket és véletlenszerűen választ számokat).
- Skip / jump funkció: lehetőség a sáv bizonyos pontjára ugrani.
- Pontszám és sorozat (streak) követése; megfelelő/hibás válaszokra hangjelzés (public/correct.wav és public/wrong.wav használata).
- Google AdSense integráció, azonban a reklámok megjelenítése nem működik a Google által megkövetelt feltételek miatt (pl. érvényes domain és HTTPS szükséges).
- Zene cím és előadó váltogatva kérdezve.
- Egyszerű, letisztult UI a Vite + React használatával.
- Last.fm integráció a leghallgatottabb számok és playlistek játékba helyezéséhez.
- Profilmenü ahol a felhasználó megtekintheti a statisztikáit, és kijelentkezhet a Google fiókjából.

Rendszerkövetelmények

- Java 21+
- Node.js 18+ és npm
- Docker és Docker Compose
- Maven
- PostgreSQL (Docker Compose-ban konfigurálva)

Rövid használat

1) Frontend (projekt gyökérben):

```bash
cd frontend
npm install
npm run build
npx serve -s dist -l 5173
```

2) Backend (projekt gyökérben):

A projekt futtatásához szükséges Docker és Maven telepítése. A backend elindításához először a Docker Compose segítségével el kell indítani a szükséges szolgáltatásokat (pl. PostgreSQL adatbázis), majd a Maven Wrapper segítségével (mvnw.cmd Windows-on, ./mvnw Linux/macOS-on) elindítani a Spring Boot alkalmazást a megfelelő profil használatával:

```bash
docker-compose up -d
cd backend
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=postgres"
# Linux/macOS
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=postgres"
```

3) Tesztek

Frontend (Vitest):

```bash
cd frontend
npm run test
```

Backend (Maven + JUnit):

```bash
cd backend
# Windows
.\mvnw.cmd test
# Linux/macOS
./mvnw test
```

Biztonsági konfigurálás

A backend a következő biztonsági intézkedéseket tartalmazza:

- Route-szintű JWT protection: a `/api/stats/**`, `/api/auth/me` és `/api/deezer/**` végpontok hitelesítést igényelnek.
- Google OAuth audience (aud) validáció: a Google ID tokenok a saját Client ID-nkhoz kell, hogy kiadottak legyenek.
- JWT Secret validáció: production módban (`-Dspring.profiles.active=prod`) kötelezően szükséges az `app.auth.jwtSecret` environment változó.
- Development módban (default) plain-token módban működik a hitelesítés gyorsabb iteráció céljából.

Environment változók

Ahhoz hogy elinduljon a backend, szükség van az alábbi environment változókra az .env fájlban:

- GOOGLE_CLIENT_ID: Google OAuth Client ID
- GOOGLE_CLIENT_SECRET: Google OAuth Client Secret
- APP_LASTFM_API_KEY: Last.fm API kulcs (opciónal)
- APP_LASTFM_SECRET: Last.fm API secret (opciónal)
- JWT_SECRET: JWT aláírási kulcs (production módban kötelezően szükséges)

Végpontok

- A frontend általában: http://localhost:5173
- A backend szerver a következő címen fut: http://localhost:3002

Konzulens: Bilicki Vilmos
Készítette: Szabó Ádám

