# Adatmodell — TuneTrivia

## Fő entitások
| Entitás | Felelősség | Fontos mezők | Kapcsolatok | Validáció | Megjegyzés |
|---|---|---|---|---|---|
| User | Felhasználói fiók | id (UUID), name, email, pictureUrl, providerId | PlayStat | email formátum, providerId egyedi | Google OAuth támogatás |
| Quiz/PlayStat | prompt non-empty, answers.length == 4 | clipUrl szerveroldali hostolt snippet lehetséges |
| PlayStat | Mentett eredmény | id, userId, quizId, score, correctCount, durationMs, timestamp | User | score >= 0 | statisztikák alapja |

## Döntési indoklások
- Tárolás: PostgreSQL (relációs), mivel tranzakciók és kapcsolatok egyszerűen kezelhetők, Flyway migrációk már a projektben megtalálhatók.
- Cache: rövid távú kérdéscache a backendben a késleltetés csökkentésére.
