"""Outline the mark's numerals for the launcher icon.

A vector drawable cannot set type, so the launcher icon carries the four
numerals as outlines of the typeface the in-app logo sets them in: Google Sans
at bold, with tabular figures, 28 units tall on the 512 canvas. Each is placed
the way the app places text, its line box centred on its ring.

    python3 numerals.py 'GoogleSans[GRAD,opsz,wght].ttf'

The font is the variable Google Sans from the Google Fonts repository
(ofl/googlesans, SIL Open Font License). Needs fontTools. Prints one path per
numeral, in the canvas coordinates the drawables use: origin at the centre,
y growing downwards.
"""

import sys

from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

NUMERAL_SIZE = 28
RING_DISTANCE = 78
LABELS = [("10", 0, -RING_DISTANCE), ("11", RING_DISTANCE, 0),
          ("46", 0, RING_DISTANCE), ("33", -RING_DISTANCE, 0)]


def tabular(font):
    """The glyph names the tnum feature substitutes for the default figures."""
    gsub = font["GSUB"].table
    swaps = {}
    for record in gsub.FeatureList.FeatureRecord:
        if record.FeatureTag != "tnum":
            continue
        for index in record.Feature.LookupListIndex:
            for table in gsub.LookupList.Lookup[index].SubTable:
                table = getattr(table, "ExtSubTable", table)
                swaps.update(getattr(table, "mapping", {}))
    return swaps


def outline(font, text, centre_x, centre_y):
    glyphs = font.getGlyphSet()
    cmap = font.getBestCmap()
    swaps = tabular(font)
    names = [swaps.get(cmap[ord(c)], cmap[ord(c)]) for c in text]

    scale = NUMERAL_SIZE / font["head"].unitsPerEm
    ascent, descent = font["hhea"].ascent, font["hhea"].descent
    width = sum(font["hmtx"][name][0] for name in names) * scale
    x = centre_x - width / 2
    baseline = centre_y + (ascent + descent) * scale / 2

    pen = SVGPathPen(glyphs, ntos=lambda v: f"{v:.2f}".rstrip("0").rstrip("."))
    for name in names:
        glyphs[name].draw(TransformPen(pen, (scale, 0, 0, -scale, x, baseline)))
        x += font["hmtx"][name][0] * scale
    return pen.getCommands()


def main(path):
    font = instantiateVariableFont(TTFont(path), {"wght": 700, "opsz": 18, "GRAD": 0})
    for text, x, y in LABELS:
        print(f"{text}\t{outline(font, text, x, y)}")


if __name__ == "__main__":
    main(sys.argv[1])
