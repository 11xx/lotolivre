# LotoLivre

Android app showing Brazilian federal lottery (Caixa) results. Results only —
no bet generator, no statistics tab, no saved games, no ads, no tracking, no
analytics. `INTERNET` is the only permission it needs today; sharing a result
as a picture goes out through a FileProvider rather than storage access.

Kotlin + Jetpack Compose + Material 3, single `:app` module, offline-first.

## Build and verify

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Gradle pins its own daemon JVM through `gradle/gradle-daemon-jvm.properties`,
so no `JAVA_HOME` setup is required regardless of the host's default JDK.

## Data source

Every result comes from the Caixa portal API. No key, no auth, no required
headers.

```
GET https://servicebus2.caixa.gov.br/portaldeloterias/api/{modalidade}
GET https://servicebus2.caixa.gov.br/portaldeloterias/api/{modalidade}/{concurso}
```

`{modalidade}` is one of `maismilionaria`, `megasena`, `lotofacil`, `quina`,
`lotomania`, `timemania`, `duplasena`, `federal`, `diadesorte`, `supersete`,
`loteca`.

All eleven return the same JSON shape. Games differ only in which optional
fields are populated, so one DTO covers every modality and the per-game
difference belongs in the mapping to the domain model, not in the wire types.

| Modality | Carries |
| --- | --- |
| Mega-Sena, Lotofácil, Quina, Lotomania | `listaDezenas` alone |
| Dupla Sena | plus `listaDezenasSegundoSorteio` |
| +Milionária | plus `trevosSorteados` |
| Timemania, Dia de Sorte | `nomeTimeCoracaoMesSorte` (team / month) |
| Super Sete | `listaDezenas` is seven single digits, one per column |
| Federal | `listaDezenas` is five six-digit ticket numbers |
| Loteca | `listaDezenas` is null; results are in `listaResultadoEquipeEsportiva` |

Behaviours that must be handled explicitly, each one a defect if missed:

- An out-of-range concurso answers **HTTP 500**, not 404.
- `nomeTimeCoracaoMesSorte` returns seventeen NUL bytes where it does not
  apply, so a null check alone does not screen it out.
- Money arrives as JSON doubles. Parse through the string form into
  `BigDecimal`; never format currency from a `Double`.
- Dates are `dd/MM/yyyy` strings, and `dataProximoConcurso` may be absent.
  minSdk 26 makes `java.time` available for this without desugaring.
- Results are immutable once drawn, so the cache never needs invalidation.
- The endpoint is undocumented and sits behind a CDN that sets fingerprinting
  cookies. Keep it behind an interface so a mirror can replace it without the
  UI noticing.

## Naming and trademarks

The game names — Mega-Sena, Lotofácil, Quina and the rest — are marks of Caixa
Econômica Federal, used here descriptively to label the results they name. All
rights in them remain with their respective owners. The app is not affiliated
with, endorsed by, or connected to Caixa, and both the store listing and the
in-app About screen state that plainly.

Ship no Caixa logos or visual-identity assets. Game colours are fine. Never
request credentials and never facilitate betting.

The disclaimer belongs in the About screen and the store listing, and nowhere
else. A shared picture deliberately carries none: the layout is the app's own
work, the numbers in it are public record, and neither borrows anything that
needs disclaiming. Stamping a legal notice on every result someone sends a
friend would buy nothing and cost the picture. Note that an unused disclaimer
string reads like an oversight to anyone reviewing lint, so there is none to
find — the wording lives only where it is shown.

Display names live in exactly one place, so changing or abbreviating any of
them is a single edit. This is ordinary localisation hygiene rather than a
precaution — the same seam serves translation, short labels in compact
layouts, and any later change in how a game is labelled.

`applicationId` is `org.x11xx.loterias` and deliberately carries no brand name.
It is immutable once published, so keeping the brand out of it means renaming
the app costs a string change rather than a new store listing.

## Conventions

- Keep composables dumb and state hoisted, with styling confined to the theme
  and a per-game colour map, so the visual design can be replaced without
  touching data or navigation code. `design/` holds the artboards the screens
  follow, and the decisions they encode.
- Prefer OkHttp with kotlinx.serialization directly. Two endpoints do not earn
  a Retrofit interface.
- Manual dependency wiring through a single container. No DI framework.
- Model the per-game draw shape as a sealed type mapped once from the DTO;
  adding a future game should be one new branch.
- Test the mapper against saved JSON fixtures for all eleven modalities. That
  mapping is where essentially all the correctness risk lives.
- Responses are cached as raw JSON, keyed by modality and contest. Nothing is
  ever invalidated, and the newest contest per game is recorded beside them.
- A drawn result cannot change, so a request can only discover a contest drawn
  since. A game whose next draw is still ahead is not asked about at all, and
  no game is asked twice inside a minute however often the list is pulled.
- A result shared as a picture is composed at a density of its own, so the
  image does not vary with the screen that made it.
- Each game's colour is derived from one hue in OKLCH at a lightness and chroma
  the theme fixes for every game, rather than eleven palettes maintained twice.
- One type family, downloaded through the font provider rather than packaged,
  with the platform sans as the fallback. Figures are tabular everywhere.
- The mark is one geometry drawn twice: an adaptive launcher icon that carries
  only what survives at launcher sizes, and a Compose logo that draws it in
  full on its own canvas. `design/logo/` holds the masters and the measures.
  It borrows nothing from Caixa's visual identity.

## License

Public domain under the Unlicense.
