# Use case specifikáció — TuneTrivia

## Use case lista (aktuális)
| ID | Név | Aktor | Rövid cél | Prioritás | Érintett követelmények |
|---|---|---|---|---|---|
| UC-01 | Felhasználó bejelentkezése | Vendég | Google OAuth belépés és profil elérése | Must | FK-01 |
| UC-02 | Egyjátékos kvíz indítása | Vendég / bejelentkezett | Játék elindítása, kérdések megválaszolása | Must | FK-02 |
| UC-03 | Eredmény megtekintése | Játékos | Játék eredmények és statisztika megtekintése | Should | FK-03 |

## Részletes use case: UC-02 — Egyjátékos kvíz indítása
- ID: UC-02
- Elsődleges aktor: Vendég vagy bejelentkezett felhasználó
- Előfeltétel: Backend képes kérdéseket szolgáltatni (Deezer/LastFm/előre töltött adat)
- Trigger: A felhasználó a „Start” gombra kattint
- Fő sikeres lefutás: 1) Quiz létrejön; 2) x kérdés betöltése; 3) kérdés + opciók megjelenítése; 4) válaszválasztás és visszajelzés; 5) eredmény összegzése és (ha bejelentkezett) mentése
- Alternatív lefutás: kérdésbetöltés sikertelen → hibaüzenet és retry; mentés sikertelen → részleges mentés vagy újrapróbálkozás
- Jogosultsági szempontok: vendég játéka nem feltétlenül mentődik; bejelentkezett felhasználónál PlayStat mentés történik
