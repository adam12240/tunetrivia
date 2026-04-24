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

Rövid használat

1) Frontend (projekt gyökérben):

```bash
cd frontend
npm install
npm run build
npx serve -s dist -l 5173
```

2) Backend (projekt gyökérben):
A projekt futtatásához szükséges Docker és Maven telepítése.
A backend elindításához először a Docker Compose segítségével el kell indítani a szükséges szolgáltatásokat (pl. PostgreSQL adatbázis), majd a Maven segítségével elindítani a Spring Boot alkalmazást a megfelelő profil használatával.

```bash
docker compose up -d
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Ahhoz hogy elinduljon a backend, mindenképp szükség van három .env fájlra, az example.env fájl alapján, amely tartalmazza a szükséges környezeti változókat (pl. Google OAuth, adatbázis hitelesítő adatok).
- A frontend általában: http://localhost:5173
- A backend szerver a következő címen fut: http://localhost:3002

Konzulens: Bilicki Vilmos
Készítette: Szabó Ádám

