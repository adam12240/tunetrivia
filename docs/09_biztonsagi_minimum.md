# Biztonsági minimum — TuneTrivia

## Ellenőrző lista és állapot (projekt)
| Terület | Ellenőrzött intézkedés | Bizonyíték / hol található | Állapot |
|---|---|---|---|
| XSS | Frontend minimalizálása és értékek escaped megjelenítése | `frontend/src` komponensek, alapvető sanitization | Részben: audit javasolt |
| Injection | Spring Data / parametrizált lekérdezések a backenden | `backend/src/main/java` repo | Implementált: ORM használat |
| Credential | Titkok környezeti változókban, `.env.example` a frontendben | `.env.example` hiányzik: figyelem | Javítandó: .env.example létrehozása frontendhez/backendhez 
| OAuth | Id token validálása a backendben | `AuthService.verifyGoogleIdToken` | Implementált |
| Access control | Egyszerű role-check az admin műveleteknél (nem minden endpointra) | részleges | Részben: további endpointoknál szükséges |

## Konkrét óvintézkedések a projektben
- Google client secret nem kerül a frontendbe.
- Avatar proxy csak engedett hostokról tölt képeket, és ellenőrzi a response content-type-ot.
- Parametrizált DB lekérdezések és Spring Data használata véd az SQL injection ellen.


