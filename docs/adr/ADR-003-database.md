# ADR-003: Adatbázis — PostgreSQL

Döntés
-------
A projekt relációs adatbázisként PostgreSQL-t használ.

Kontextus
---------
- Flyway migrációk már megtalálhatók a `backend` projektben.
- Postgres jól támogatott Java ökoszisztémában és megbízható tranzakciókezelést biztosít.

Alternatívák
-----------
- MySQL: hasonló képességek, de Postgres sajátos JSONB és sampling funkciói hasznosak lehetnek.
- NoSQL (MongoDB): flexibilis séma, de a kapcsolatok és tranzakciók kezelése bonyolultabb lehet.

Döntés és indoklás
------------------
Postgres választva a relációs integritás, Flyway és a projekt igényei miatt.

Következmények
---------------
- Használjuk a Postgres-specifikus mintavételi javaslatokat a véletlenszerű kérdésválasztáshoz (pl. precomputed shuffle mező vagy sampling megközelítés).

Validálás
---------
- A Docker Compose szolgáltatásként indít Postgres-t, és a backend migrációk lefutnak a konténerindításkor.


