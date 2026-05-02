# Funkcionális és nem funkcionális követelmények — TuneTrivia

## Funkcionális követelmények (aktuális állapot)
| ID | Követelmény | Felhasználói érték | Prioritás | Elfogadási kritérium | Kapcsolódó use case | Kapcsolódó képernyő | Teszt ID |
|---|---|---|---|---|---|---|---|
| FK-01 | Google OAuth alapú belépés és profil lekérése | Gyors és megbízható belépés, személyre szabott statisztikák | Must | Sikeres idToken ellenőrzés a backendben, profil adatok elérhetők | UC-01 | SCR-HOME | TC-AUTH-01 |
| FK-02 | Egyjátékos kvíz indítása és futtatása (10 kérdés) | Az alap játékélmény működése | Must | Quiz indítása → 10 kérdés, válaszadási logika, eredményoldal | UC-02 | SCR-QUIZ | TC-GAME-01 |
| FK-03 | Játék eredmények mentése PlayStat táblába | Felhasználói teljesítmény nyomon követése | Should | Mentett PlayStat tartalmaz score-t, correctCount-ot | UC-03 | SCR-RESULTS | TC-GAME-02 |

## Nem funkcionális követelmények (aktuális állapot)
| ID | Minőségi attribútum | Követelmény | Mérési mód | Célérték | Kapcsolódó teszt |
|---|---|---|---|---|---|
| NFK-01 | Teljesítmény | Quiz start p95 < 1.5 s lokálisan | mérés automatizált script | p95 < 1.5 s | TC-PERF-01 |
| NFK-02 | Biztonság | Nincsenek titkok a frontend forráskódban | secret-scan | nincs találat | TC-SEC-01 |
| NFK-03 | Megbízhatóság | API hívások 99% sikeresek normál terhelésnél | monitoring | 99% | TC-INTEG-01 |

## Követelménykövetés
Ezek a táblák tükrözik a jelenlegi projekt állapotát; a fejlesztés során a FK/NFK táblák frissülnek.
