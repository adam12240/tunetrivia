# ADR-004: Hitelesítés — Google OAuth + Backend token validáció

Döntés
-------
A felhasználói hitelesítés Google OAuth alapokon történik; a frontend kap egy idToken-t, amit a backend ellenőriz és saját session/cookie vagy JWT alapján kezel.

Kontextus
---------
- A projekt célja egyszerű és megbízható bejelentkezés.
- A backend-ben található `AuthService` már képes Google idToken ellenőrzésre.

Alternatívák
-----------
- Egyszerű e-mail/jelszó regisztráció: nagyobb fejlesztési költség és jelszókezelési felelősség.
- Teljes OAuth szerver implementáció (pl. Keycloak): túl sok overhead az MVP-hez.

Döntés és indoklás
------------------
Google OAuth választva az egyszerű UX és a megbízhatóság miatt. A backend validálja az idToken-t és szükség szerint saját auth cookie-t állít be.

Következmények
---------------
- Fontos: a Google client secret soha nem kerül a frontendbe; a backend környezetben kell tárolni.
- Refresh token stratégia és cookie beállítások (Secure, HttpOnly, SameSite) dokumentálva vannak a kódban.



