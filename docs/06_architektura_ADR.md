# Architektúra és ADR — TuneTrivia

## Fő minőségi célok
| Attribútum | Elvárás | Következmény a tervben |
|---|---|---|
| Biztonság | Google OAuth, tokenkezelés | tokenek backend-validációja, titkok kezelése környezeti változókban |
| Karbantarthatóság | Frontend/backed szeparáció | REST API, jól definiált DTO-k, rétegzett szolgáltatások |
| Tesztelhetőség | Unit és integrációs tesztek | szolgáltatás-elszigetelés, testcontainer használat a DB-hez |

## Fontos architekturális döntések (ADRs)
| ADR ID | Cím | Döntés összefoglalója | Döntés helye a repo-ban |
|---|---|---|---|
| ADR-001 | Frontend keretrendszer | React + Vite megtartása a meglévő kódbázis és gyors fejlesztés miatt. | `docs/adr/ADR-001-frontend-framework.md` |
| ADR-002 | Backend keretrendszer | Fenntartjuk a Spring Bootot a backendhez (Java, Maven, Spring Data, Flyway). | `docs/adr/ADR-002-backend-framework.md` |
| ADR-003 | Adatbázis | PostgreSQL használata relációs tároláshoz és Flyway migrációkhoz. | `docs/adr/ADR-003-database.md` |
| ADR-004 | Hitelesítés | Google OAuth (idToken ellenőrzés a backendben) és cookie-based session kezelés. | `docs/adr/ADR-004-auth.md` |
| ADR-005 | Kérdés-/zenei forrás | Deezer/Last.fm proxy kombinálva előre feldolgozott, szerveren tárolt kérdésekkel (MVP kompromisszum). | `docs/adr/ADR-005-content-source.md` |

A fenti ADR fájlok részletes leírásai a `docs/adr/` mappában találhatók.
