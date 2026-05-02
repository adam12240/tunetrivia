# Piaci / területi elemzés — TuneTrivia

## Rövid összefoglaló
A TuneTrivia célja rövid, zenei felismerési kvízek egy webes felületen: gyors körök, egyszerű belépés (Google OAuth), és alapstatisztikák a felhasználói teljesítményről. A megoldás a casual gamerek és baráti társaságok igényeire fókuszál.

## Versenytér (kiemelt példák)
- Heardle / SongGuess típusú webjátékok: egyszerű zenefelismerés, gyors körök. Erősségük a könnyű belépés és rövid játék. Hiányosság: kevés személyre szabott adat, általában nincs beépített statisztika.
- Spotify/streaming-integrált játékok: hozzáférés nagy zenei adatbázishoz, de API-korlátozások és jogi kérdések lehetnek.
- Mobil kvíz alkalmazások: gazdag UX és social funkciók, de fejlesztési költségek és disztribúciós overhead nagyobb.

## Miből tanultunk és miért így építettük
- Egyszerű, megbízható belépés fontos: ezért Google OAuth kliens oldali integráció + szerveroldali token ellenőrzés.
- A zenei tartalmak jogi és API limitációi miatt az MVP nem próbál teljes Spotify integrációt; helyette Deezer/Last.fm proxy és előre feldolgozott források használata csökkenti a kockázatot.
- A mérhető érték: gyors játékindítás és per-játék statisztikák — ezek a felhasználói megtartás első lépcsői.

## Következtetések TuneTrivia-re
- Megtartott funkciók az MVP-ben: Google alapú bejelentkezés, 1-játékos kvízfolyamat, eredménymentés és alap statisztikák.
- Kihagyott / későbbre tett funkciók: teljes streaming-szintű integráció (Spotify), mély ajánlórendszer, komplex social megosztás.
- UX/biztonság tanulság: ne tároljunk titkokat kliensoldalon; a felhasználói médiafájlok proxyzása host-whitelisttel és tartalomtípus ellenőrzéssel történik.

## Források és hivatkozások
- Project repo (ez a dokumentáció része) — kód és migrációk a `backend` mappában.
- Harmadik fél szolgáltatások dokumentációi: Last.fm API, Deezer API (linkek a dolgozatban megtalálhatók).
