# TuneTrivia

Egyszerű, Vite + React alapú zenei kvíz alkalmazás Deezer API proxy-val.

Funkciók

- Különböző játékmódok: rövid (klasszikus) játék és végtelen módban történő folyamatos játék.
- Műfaj szerinti válogatás (a frontend a proxy-n keresztül tölti be a Deezer top trackjeit egy adott műfajból).
- Véletlenszerű kérdések a lekért számokból (a proxy szűri a nem ASCII karaktereket és véletlenszerűen választ számokat).
- Skip / jump funkció: lehetőség a sáv bizonyos pontjára ugrani.
- Pontszám és sorozat (streak) követése; megfelelő/hibás válaszokra hangjelzés (public/correct.wav és public/wrong.wav használata).

Rövid használat

1) Frontend (projekt gyökérben):

```bash
npm install
npm run dev
```

2) Deezer proxy (külön terminál):

```bash
cd deezer-proxy
npm install
node server.js
```

- A frontend általában: http://localhost:5173
- A proxy alapértelmezett útvonala: http://localhost:3001/deezer

Készítette: Szabó Ádám
Konzulens: Bilicki Vilmos
