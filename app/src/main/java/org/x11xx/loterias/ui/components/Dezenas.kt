package org.x11xx.loterias.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.ui.theme.GameColors

/**
 * A drawn dezena. Circles everywhere, whatever the game or the count. The
 * numeral is sized from the ball rather than the reader's font size, so it
 * always fits; the ball is what grows for a larger font.
 */
@Composable
fun Ball(
    label: String,
    colors: GameColors,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = colors.onAccent,
            fontSize = with(LocalDensity.current) { (size * 0.44f).toSp() },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Super Sete's digits stand in a seven-track grid because each belongs to a
 * numbered column; a circle could not fill its track without leaving the
 * column labels adrift.
 */
@Composable
fun ColumnDigit(
    column: Int,
    digit: String,
    colors: GameColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = column.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.accent)
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = digit,
                color = colors.onAccent,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * A trevo carries its number inside the leaf, the way the game presents it.
 * Four overlapping discs read as a clover at sizes where an outline would
 * collapse into a smudge.
 */
@Composable
fun CloverToken(
    label: String,
    colors: GameColors,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                val radius = this.size.minDimension * 0.25f
                val offset = this.size.minDimension * 0.19f
                val centre = Offset(this.size.width / 2f, this.size.height / 2f)
                val clover = Path().apply {
                    listOf(
                        Offset(centre.x - offset, centre.y - offset),
                        Offset(centre.x + offset, centre.y - offset),
                        Offset(centre.x - offset, centre.y + offset),
                        Offset(centre.x + offset, centre.y + offset),
                    ).forEach { addOval(Rect(it, radius)) }
                    // Each disc overlaps its two neighbours but none reaches the
                    // point all four surround, so the square spanning their
                    // centres closes the pinhole that would otherwise sit there.
                    addRect(
                        Rect(
                            left = centre.x - offset,
                            top = centre.y - offset,
                            right = centre.x + offset,
                            bottom = centre.y + offset,
                        ),
                    )
                }
                drawPath(clover, colors.accent)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = colors.onAccent,
            fontSize = with(LocalDensity.current) { (size * 0.44f).toSp() },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * A Loteca draw is a drawn cross rather than the letter X, which rises past
 * the figures beside it and unsettles the row.
 */
@Composable
fun DrawnCross(colour: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                val inset = this.size.minDimension * 0.28f
                val stroke = Stroke(width = this.size.minDimension * 0.13f, cap = StrokeCap.Round)
                drawLine(
                    colour,
                    Offset(inset, inset),
                    Offset(this.size.width - inset, this.size.height - inset),
                    stroke.width,
                    stroke.cap,
                )
                drawLine(
                    colour,
                    Offset(this.size.width - inset, inset),
                    Offset(inset, this.size.height - inset),
                    stroke.width,
                    stroke.cap,
                )
            },
    )
}

/** The leading gutter that keeps a card's rows sharing one left edge. */
@Composable
fun RowLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.width(22.dp),
    )
}

@Composable
fun LabelledRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        RowLabel(label)
        content()
    }
}
