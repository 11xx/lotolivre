# Logo source

The app's mark: four rings woven into a clover around a gold star, each ring
holding one of the numbers **10, 11, 33 and 46**, which sum to a hundred. It
carries no Caixa logo or visual-identity asset, and names no game.

`logo-celtic-dark.svg` and `logo-celtic-light.svg` are the masters. The rest
derive from them: `logo-celtic-transparent.svg` drops the canvas,
`logo-celtic-themed-monochrome.svg` reduces the mark to the silhouette Android
tints from the wallpaper, `favicon.svg` is the same mark tuned for 48px, and
the two wordmarks set it beside the name. `numerals.py` outlines the numerals
for the launcher icon, which cannot set type.

## Geometry

Everything is laid out on a 512 canvas centred on the origin, at a corner
radius of 115.

| Measure | Value |
| --- | --- |
| Ring centre from origin | 78 |
| Ring outer radius | 70 |
| Ring hole radius | 36 |
| Inlay stroke radius | 53 |
| Weave notch width | 8 |
| Numeral size | 28 |
| Star radius | 16 |

Two clearances follow from those numbers and are the reason they were chosen.
Adjacent ring centres are 78√2 ≈ 110.31 apart, so a ring's edge stops 4.31
short of its neighbour's hole and no number is ever crowded; and the origin is
78 out from every ring centre, past all four outer radii, so the star sits in
negative space without anything cut away for it.

## Where it is drawn

- **Launcher icon** — `app/src/main/res/drawable/ic_launcher_*.xml`. The mark
  in full, at the masters' proportions: their 512 canvas spans the icon's
  visible 72dp, which keeps the guide ring inside the safe zone. The numerals
  are outlines of the typeface the in-app logo sets them in, Google Sans at
  bold with tabular figures, placed the way the app places text. The canvas,
  guide ring and numerals follow the system theme through `@color/logo_canvas`,
  `logo_guide` and `logo_ink`, the in-app logo's two palettes; the rings are
  the same either way, which is true of the masters too. The themed icon is the
  monochrome master's silhouette: rings, star and numerals.
- **In app** — `LotoLivreLogo`, which draws the mark in full, numerals
  included, on its own canvas rather than the surface behind it.

The two are separate renderings of the same geometry, so a change to the
measures above belongs in both.

## Typeface source

The launcher numerals use Google Sans **14.000**, bold at optical size 18,
with tabular figures. The source is
[`GoogleSans[GRAD,opsz,wght].ttf`](https://github.com/google/fonts/blob/main/ofl/googlesans/GoogleSans%5BGRAD%2Copsz%2Cwght%5D.ttf),
Git blob `a5e76cc713206f1cfe9a7fc31cc1b6f035034d81`, under the
[SIL Open Font License 1.1](https://github.com/google/fonts/blob/main/ofl/googlesans/OFL.txt).
`numerals.py` generates the four paths used in both launcher drawables.

The font is downloaded by the app's font provider, with the system sans as
fallback; no font binary is bundled. The outlined logo is artwork:
[OFL FAQ 1.1–1.1.2](https://openfontlicense.org/ofl-faq/) permits this use
without changing the artwork's license.
