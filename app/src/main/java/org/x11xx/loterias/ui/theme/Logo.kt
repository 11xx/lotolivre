package org.x11xx.loterias.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app's mark: four rings woven into a clover around a gold star, each ring
 * holding one of the numbers 10, 11, 33 and 46, which sum to a hundred.
 *
 * It draws its own canvas rather than sitting on the surface behind it, so the
 * punch-holes, the weave notches and the numerals keep the contrast the mark
 * was drawn for wherever it is placed. That canvas is a rounded square in the
 * proportions of a launcher icon, which is what it is.
 */
@Composable
fun LotoLivreLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    dark: Boolean = true,
    contentDescription: String? = null,
) {
    val measurer = rememberTextMeasurer()
    val palette = if (dark) DarkLogo else LightLogo

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription?.let { this.contentDescription = it } },
    ) {
        val unit = this.size.minDimension / CANVAS

        drawRoundRect(
            color = palette.canvas,
            size = this.size,
            cornerRadius = CornerRadius(CORNER * unit),
        )

        scale(scale = unit, pivot = Offset.Zero) {
            translate(left = CANVAS / 2f, top = CANVAS / 2f) {
                drawCircle(
                    color = palette.guide,
                    radius = GUIDE_RADIUS,
                    center = Offset.Zero,
                    style = Stroke(width = 2f),
                )
                Leaf.entries.forEach { drawLeaf(it, palette.canvas) }
                drawCircle(color = palette.canvas, radius = STAR_CLEARANCE, center = Offset.Zero)
                drawPath(path = starPath(), color = Star)
            }
        }

        Leaf.entries.forEach { leaf ->
            val layout = measurer.measure(
                text = leaf.label,
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontFeatureSettings = "tnum",
                    fontWeight = FontWeight.Bold,
                    fontSize = (NUMERAL_SIZE * unit).toSp(),
                    color = palette.ink,
                ),
            )
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = (CANVAS / 2f + leaf.x) * unit - layout.size.width / 2f,
                    y = (CANVAS / 2f + leaf.y) * unit - layout.size.height / 2f,
                ),
            )
        }
    }
}

/**
 * One ring per quadrant, drawn in order. Each carries the notches that cut it
 * into the rings already laid down, which is what makes the four read as woven
 * rather than stacked; the last one also notches the first to close the cycle.
 */
private enum class Leaf(
    val x: Float,
    val y: Float,
    val label: String,
    val fill: Color,
    val inlay: Color,
    val notches: List<String>,
) {
    TOP(0f, -RING_DISTANCE, "10", Color(0xFF347C5A), Color(0xFF95D5B2), emptyList()),
    RIGHT(RING_DISTANCE, 0f, "11", Color(0xFF2D6A4F), Color(0xFF74C69D), listOf(
        "M -70,0 A 70,70 0 0,1 -20,-60",
    )),
    BOTTOM(0f, RING_DISTANCE, "46", Color(0xFF1B4332), Color(0xFF2D6A4F), listOf(
        "M 0,-70 A 70,70 0 0,1 60,-20",
    )),
    LEFT(-RING_DISTANCE, 0f, "33", Color(0xFF235840), Color(0xFF52B788), listOf(
        "M 0,70 A 70,70 0 0,1 20,60",
        "M 70,0 A 70,70 0 0,1 20,-60",
    )),
}

private fun DrawScope.drawLeaf(leaf: Leaf, canvas: Color) {
    translate(left = leaf.x, top = leaf.y) {
        leaf.notches.forEach { notch ->
            drawPath(
                path = PathParser().parsePathString(notch).toPath(),
                color = canvas,
                style = Stroke(width = NOTCH_WIDTH, cap = StrokeCap.Butt),
            )
        }

        val ring = Path().apply {
            fillType = PathFillType.EvenOdd
            addOval(Rect(Offset.Zero, RING_OUTER))
            addOval(Rect(Offset.Zero, RING_INNER))
        }
        drawPath(path = ring, color = leaf.fill)

        drawCircle(
            color = leaf.inlay.copy(alpha = 0.6f),
            radius = INLAY_RADIUS,
            center = Offset.Zero,
            style = Stroke(width = 2.5f),
        )
    }
}

/** Four cusps pulled to the centre, so the star reads at any size. */
private fun starPath(): Path = Path().apply {
    moveTo(0f, -STAR_RADIUS)
    quadraticTo(0f, 0f, STAR_RADIUS, 0f)
    quadraticTo(0f, 0f, 0f, STAR_RADIUS)
    quadraticTo(0f, 0f, -STAR_RADIUS, 0f)
    quadraticTo(0f, 0f, 0f, -STAR_RADIUS)
    close()
}

private class LogoPalette(val canvas: Color, val guide: Color, val ink: Color)

private val DarkLogo = LogoPalette(
    canvas = Color(0xFF0B0F0D),
    guide = Color(0xFF1A2922),
    ink = Color(0xFF74C69D),
)

private val LightLogo = LogoPalette(
    canvas = Color(0xFFF2F5F0),
    guide = Color(0xFFD1DDD5),
    ink = Color(0xFF0F291E),
)

private val Star = Color(0xFFD4AF37)

// The mark is laid out on a 512 canvas centred on the origin. Ring centres sit
// 78 out, which leaves 4.31 between a ring's hole and its neighbour's edge, so
// no ring ever intrudes on a number, and puts the origin clear of all four.
private const val CANVAS = 512f
private const val CORNER = 115f
private const val GUIDE_RADIUS = 190f
private const val RING_DISTANCE = 78f
private const val RING_OUTER = 70f
private const val RING_INNER = 36f
private const val INLAY_RADIUS = 53f
private const val NOTCH_WIDTH = 8f
private const val NUMERAL_SIZE = 28f
private const val STAR_RADIUS = 16f
private const val STAR_CLEARANCE = 22f
