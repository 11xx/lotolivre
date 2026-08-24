# Logo source

The app's mark: four rings woven into a clover around a gold star, each ring
holding one of the numbers **10, 11, 33 and 46**, which sum to a hundred. It
carries no Caixa logo or visual-identity asset, and names no game.

`logo-celtic-dark.svg` and `logo-celtic-light.svg` are the masters. The rest
derive from them: `logo-celtic-transparent.svg` drops the canvas,
`logo-celtic-themed-monochrome.svg` reduces the mark to the silhouette Android
tints from the wallpaper, `favicon.svg` is the same mark tuned for 48px, and
the two wordmarks set it beside the name.

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

- **Launcher icon** — `app/src/main/res/drawable/ic_launcher_*.xml`. The
  numerals, the notches and the inlay strokes fall under a pixel at launcher
  sizes, so the adaptive icon carries the rings, the holes and the star alone,
  scaled to span the 66dp safe zone. Its canvas follows the system theme
  through `@color/logo_canvas`; the rings are the same either way, which is
  true of the masters too.
- **In app** — `LotoLivreLogo`, which draws the mark in full, numerals
  included, on its own canvas rather than the surface behind it.

The two are separate renderings of the same geometry, so a change to the
measures above belongs in both.
