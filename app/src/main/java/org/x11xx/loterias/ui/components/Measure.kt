package org.x11xx.loterias.ui.components

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The space between two balls, across a row and between rows. */
val BallGap = 7.dp

/** The most balls a row holds before the draw takes another row. */
const val MaxBallsPerRow = 8

/**
 * The widest a screen's content grows. Past it a prize table's figures drift
 * too far from their labels to read across.
 */
val ReadableWidth = 600.dp

/**
 * [items] in rows of at most [max], as even as the count allows: fifteen by
 * eight is eight and seven, twenty is seven, seven and six. The rows depend on
 * the count alone, so a draw breaks the same way at any width.
 */
fun <T> List<T>.inEvenRows(max: Int): List<List<T>> {
    if (isEmpty()) return emptyList()
    val rows = (size + max - 1) / max
    val base = size / rows
    val longer = size % rows
    var start = 0
    return List(rows) { index ->
        val length = if (index < longer) base + 1 else base
        subList(start, start + length).also { start += length }
    }
}

/**
 * How far a ball grows with the reader's font size. Its numeral is sized from
 * the ball, so the ball has to grow for the numeral to keep pace with the text
 * around it; past this the rows run out of width anyway.
 */
const val MaxBallGrowth = 1.3f

/** The length of the longest row [inEvenRows] makes of [count] items. */
fun longestEvenRow(count: Int, max: Int): Int {
    if (count <= 0) return 0
    val rows = (count + max - 1) / max
    return (count + rows - 1) / rows
}

/**
 * The largest ball up to [preferred] that fits [perRow] of them, gaps included,
 * in [width]. It is worked out in whole pixels because every ball and gap is
 * laid out at a whole pixel: a size worked out in dp can round up on each ball
 * and leave the last one squeezed out of round.
 */
fun fittedBallSize(preferred: Dp, width: Dp, perRow: Int, density: Density): Dp {
    if (width == Dp.Infinity || perRow <= 0) return preferred
    return with(density) {
        val room = width.roundToPx() - BallGap.roundToPx() * (perRow - 1)
        minOf(preferred, (room / perRow).coerceAtLeast(0).toDp())
    }
}

/** Side padding that centres content at [ReadableWidth] and never drops below [minimum]. */
fun readableInset(windowWidth: Dp, minimum: Dp): Dp =
    maxOf(minimum, (windowWidth - ReadableWidth) / 2)
