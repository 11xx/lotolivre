# Design source

Six artboards describing the two screens the app has — the results list and a
single game's detail — plus the navigation drawer, each in light and dark, and
the Loteca detail that renders match results instead of dezenas. They are the
source of truth for layout, spacing, colour and typography decisions; the app's
theme and composables follow them.

Each `.dc.html` is one artboard: self-contained HTML with its own token block
and sample data taken from real draws. `canvas.json` places them on a shared
canvas and carries the notes explaining the decisions they encode.

## Decisions the artboards encode

- **Dezenas are always circles.** Every game's numbers render as balls, at every
  size, so a card is recognisable at a glance regardless of how many are drawn.
  Super Sete is the exception that proves the rule: its seven digits are
  positional, one per numbered column, so they fill a seven-track grid where a
  circle could not stay aligned with its column label.
- **Card height follows what the game draws, never the prize.** Quina shows
  five, Lotomania twenty, Federal five ticket numbers, Loteca fourteen match
  results. Sizing by prize would make the list reflow week to week.
- **A draw's rows follow its count, never the width.** At most eight balls to
  a row, the rows as even as the count allows — Lotofácil eight and seven,
  Lotomania seven, seven and six, each Dupla Sena draw one row of six — and the
  balls shrink to keep a row whole. A game breaks the same way on every screen
  and in a shared picture. Where an artboard wraps its balls to the width, this
  rule wins.
- **Content keeps a readable measure.** On a wide window the list and the
  contest centre at 600dp rather than stretching a prize table's figures away
  from their labels, and on a cramped one the contest steppers fold to their
  arrows before the heading between them loses its date.
- **A total of nothing is left out.** An accumulation, estimate or revenue of
  zero says nothing, so its row is not shown; Federal has none of the three.
- **The game's colour is one hue** used at a fixed lightness and chroma for the
  balls, the prize value and a wash behind the card, computed per theme rather
  than stored twice.
- **Secondary pools get a container, not an indent.** The +Milionária trevos and
  the Federal bilhetes sit in tinted pills so they read as separate information
  without breaking the left edge of the card.
- **A drawn × marks a Loteca draw**, because the letter X rises above the
  figures' height and unsettles the row.
- **No bottom navigation.** Two destinations do not earn a bar; the drawer holds
  Resultados, Sobre and the theme choice.

## Logo

`logo/` holds the mark and the geometry it is built from, along with the two
places the app draws it.

## Rendering them

The artboards open as a pan-and-zoom canvas when seeded into the Claude Design
payload; the seeded page is a build artefact and is not tracked. Any browser
also renders a single `.dc.html` on its own once its `support.js` runtime is
present.
