package org.x11xx.loterias.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.Draw
import org.x11xx.loterias.domain.MatchOutcome
import org.x11xx.loterias.ui.theme.GameColors

/**
 * How a draw looks wherever it is shown. Every shape but Loteca's coupon reads
 * the same in a list card, on the detail screen and in a shared picture, only
 * at different sizes.
 */
@Composable
fun DrawContent(
    draw: Draw,
    colors: GameColors,
    ballSize: Dp,
    modifier: Modifier = Modifier,
    centred: Boolean = false,
) {
    when (draw) {
        is Draw.Numbers -> Balls(draw.numbers, colors, ballSize, modifier, centred)

        is Draw.TwoDraws -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LabelledRow(stringResource(R.string.label_first_draw)) {
                Balls(draw.first, colors, ballSize)
            }
            LabelledRow(stringResource(R.string.label_second_draw)) {
                Balls(draw.second, colors, ballSize)
            }
        }

        is Draw.NumbersWithClovers -> BoxWithConstraints(modifier = modifier) {
            // The trevos take the size the dezenas above them settle on.
            val clover = ballSizeFor(draw.numbers.size, ballSize, maxWidth)
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Balls(draw.numbers, colors, ballSize)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(colors.soft)
                        .padding(start = 14.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.label_clovers),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.softInk,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        draw.clovers.forEach { CloverToken(it, colors, clover) }
                    }
                }
            }
        }

        is Draw.DigitColumns -> Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            draw.digits.forEachIndexed { index, digit ->
                ColumnDigit(index + 1, digit, colors, Modifier.weight(1f))
            }
        }

        is Draw.Tickets -> Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            draw.tickets.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    pair.forEach { ticket ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(colors.soft)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "${ticket.place}º",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = ticket.number,
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.softInk,
                            )
                        }
                    }
                    if (pair.size == 1) Box(Modifier.weight(1f))
                }
            }
        }

        is Draw.Matches -> Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            draw.matches.forEach { match ->
                MatchMark(match.outcome, colors, Modifier.weight(1f))
            }
        }
    }
}

/**
 * A draw's dezenas in rows the count decides, never the width, so a draw
 * breaks the same way on every screen and in a shared picture. The balls
 * shrink to keep each row whole.
 */
@Composable
private fun Balls(
    numbers: List<String>,
    colors: GameColors,
    size: Dp,
    modifier: Modifier = Modifier,
    centred: Boolean = false,
) {
    val rows = numbers.inEvenRows(MaxBallsPerRow)
    val alignment = if (centred) Alignment.CenterHorizontally else Alignment.Start
    BoxWithConstraints(modifier = if (centred) modifier.fillMaxWidth() else modifier) {
        val ball = ballSizeFor(numbers.size, size, maxWidth)
        Column(verticalArrangement = Arrangement.spacedBy(BallGap)) {
            rows.forEach { row ->
                Row(
                    modifier = if (centred) Modifier.fillMaxWidth() else Modifier,
                    horizontalArrangement = Arrangement.spacedBy(BallGap, alignment),
                ) {
                    row.forEach { Ball(it, colors, ball) }
                }
            }
        }
    }
}

/**
 * The size a draw of [count] balls takes in [width]: [preferred], grown with
 * the reader's font size, then shrunk until the longest row fits.
 */
@Composable
private fun ballSizeFor(count: Int, preferred: Dp, width: Dp): Dp {
    val density = LocalDensity.current
    val grown = preferred * density.fontScale.coerceIn(1f, MaxBallGrowth)
    return fittedBallSize(grown, width, longestEvenRow(count, MaxBallsPerRow), density)
}

/** One cell of the Loteca coupon: home win, away win, or a drawn cross. */
@Composable
fun MatchMark(outcome: MatchOutcome, colors: GameColors, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.accent)
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (outcome) {
            MatchOutcome.DRAW -> DrawnCross(colors.onAccent, 18.dp)
            MatchOutcome.HOME -> MarkLabel("1", colors)
            MatchOutcome.AWAY -> MarkLabel("2", colors)
        }
    }
}

@Composable
private fun MarkLabel(text: String, colors: GameColors) {
    Text(
        text = text,
        color = colors.onAccent,
        style = MaterialTheme.typography.labelLarge,
    )
}
