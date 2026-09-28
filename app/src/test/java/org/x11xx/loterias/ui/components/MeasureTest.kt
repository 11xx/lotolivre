package org.x11xx.loterias.ui.components

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasureTest {

    private fun rows(count: Int) = List(count) { it }.inEvenRows(MaxBallsPerRow).map { it.size }

    @Test
    fun `every draw the portal publishes keeps one row up to eight`() {
        assertEquals(listOf(5), rows(5))
        assertEquals(listOf(6), rows(6))
        assertEquals(listOf(7), rows(7))
        assertEquals(listOf(8), rows(8))
    }

    @Test
    fun `longer draws split into rows as even as the count allows`() {
        assertEquals(listOf(5, 4), rows(9))
        assertEquals(listOf(8, 7), rows(15))
        assertEquals(listOf(7, 7, 6), rows(20))
    }

    @Test
    fun `the longest row is known from the count alone`() {
        listOf(0, 5, 8, 9, 15, 20).forEach { count ->
            assertEquals(
                "count $count",
                rows(count).maxOrNull() ?: 0,
                longestEvenRow(count, MaxBallsPerRow),
            )
        }
    }

    @Test
    fun `rows keep the draw in order`() {
        val numbers = (1..15).map { it.toString() }
        assertEquals(numbers, numbers.inEvenRows(MaxBallsPerRow).flatten())
    }

    @Test
    fun `nothing drawn is no rows`() {
        assertEquals(emptyList<List<Int>>(), emptyList<Int>().inEvenRows(MaxBallsPerRow))
    }

    @Test
    fun `a ball keeps its size while the row fits and shrinks once it does not`() {
        val one = Density(1f)
        assertEquals(46.dp, fittedBallSize(46.dp, 400.dp, 6, one))
        // Six balls with five gaps in 305dp: (305 - 35) / 6.
        assertEquals(45.dp, fittedBallSize(46.dp, 305.dp, 6, one))
        assertEquals(46.dp, fittedBallSize(46.dp, Dp.Infinity, 8, one))
    }

    @Test
    fun `a full row fits in whole pixels, the last ball as round as the first`() {
        // The shared picture: 336dp of row at 3x, eight balls of Lotofácil.
        val picture = Density(3f)
        val ball = with(picture) { fittedBallSize(46.dp, 336.dp, 8, picture).roundToPx() }
        val gap = with(picture) { BallGap.roundToPx() }
        val row = with(picture) { 336.dp.roundToPx() }
        assertTrue("8 x $ball + 7 x $gap overruns $row", 8 * ball + 7 * gap <= row)
    }

    @Test
    fun `no room gives no ball rather than a negative one`() {
        assertEquals(0.dp, fittedBallSize(46.dp, 0.dp, 6, Density(3f)))
    }

    @Test
    fun `content centres at the readable width and keeps its margin when narrower`() {
        assertEquals(16.dp, readableInset(411.dp, 16.dp))
        assertEquals(157.dp, readableInset(914.dp, 16.dp))
    }
}
