# UX és képernyőspecifikáció — TuneTrivia

## Design célok
- Gyors, egyértelmű indítás és alacsony belépési küszöb.
- Játék közbeni egyértelmű visszajelzés (időzítő, helyes/válasz kiemelés).
- Minimalista, reszponzív elrendezés mobil és desktop számára.
- Akadálymentességi alapok: fókusz-sorrend, alt/aria címkék, olvasható kontraszt.

## Fő képernyők (összegzés)
- `SCR-HOME`: Start gomb, Google bejelentkezés, profil. Állapotok: loading, authenticated.
- `SCR-QUIZ`: Kérdés, 4 válaszopció, időzítő, progress bar. Állapotok: loading, active, answered, timeout.
- `SCR-RESULTS`: Pontszám, részletes bontás, lehetőség mentésre/megosztás (MVP-ben egyszerű megjelenítés).

## SCR-QUIZ (részletes)
- Cél: gyors felismerés és egyértelmű válaszadás.
- Megjelenített elemek: kérdés szöveg, 4 válasz, hátralévő idő, aktuális pontszám.
- Interakciók: válaszválasztás, továbblépés, játék vége.
- Hibakezelés: kérdésbetöltés hiba → látható üzenet és újrapróbálkozás.
- Reszponzív: mobilon nagyobb érintési felületek, desktopon oldalsáv statisztikának.

## UX validációs eredmények
- Felhasználói tesztelés során a legtöbb résztvevő gyorsan megértette a játék menetét és könnyen navigált a képernyők között.
- Néhány visszajelzés érkezett a válaszopciók vizuális elkülönítésére.
- A hibakezelés és újrapróbálkozás logikája jól működött, de további vizuális visszajelzés hasznos lehet a betöltési állapotoknál.
- A reszponzív elrendezés jól alkalmazkodott különböző eszközökön, de a mobil érintési felületek további finomhangolást igényelhetnek a későbbi iterációkban.
- Összességében a UX és képernyőspecifikáció megfelel a projekt céljainak, és a további fejlesztések során a felhasználói visszajelzések alapján finomhangolható.
