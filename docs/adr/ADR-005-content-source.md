# ADR-005: Kérdés- és zenei forrás — Deezer/Last.fm proxy + előfeldolgozott kérdések

Döntés
-------
Az MVP zenei kérdéseit kevert megoldással szolgáljuk: Deezer és Last.fm proxy végpontokat használunk import/keresés céljából, de a kvízhez alapvető kérdéskészlet előre feldolgozott és a szerveren tárolt.

Kontextus
---------
- Streaming API-k jogi és rate-limit korlátokat jelentenek.
- A projekt tartalmaz `LastFmController` és `DeezerProxyController` implementációkat.

Alternatívák
-----------
- Teljes Spotify integráció: nehéz jogi feltételek és API korlátok miatt elhalasztva.
- Teljesen lokális kliens-oldali hangminták: növeli a frontend csomagméretet és nagyobb tárolást igényel.

Döntés és indoklás
------------------
A proxy + előfeldolgozott adatkészlet kompromisszumot ad: gyorsan induló MVP, jogi kockázat csökkentve, de bővíthetőség megőrizve.

Következmények
---------------
- Import folyamatokhoz admin eszközök javasoltak (CSV/JSON feltöltés vagy Last.fm import).
- A tartalomlimit és az adatfrissítés stratégiája dokumentálandó a dolgozatban.

Validálás
---------
- A `LastFmController` képes top-tracks betölteni és visszaadni JSON formátumban. A `DeezerProxyController` keresési és genre lekérdezéseket biztosít.


