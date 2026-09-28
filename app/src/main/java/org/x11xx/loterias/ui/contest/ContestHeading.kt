package org.x11xx.loterias.ui.contest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.R
import org.x11xx.loterias.ui.asFullDate
import java.time.LocalDate

/**
 * What contest is being read: the game named, and the draw beneath it. The
 * detail screen puts the contest steppers either side of it; the shared image
 * has nothing to step with, so it stands alone.
 *
 * The name and the line beneath it shrink rather than wrap — a long name
 * like Dia de Sorte would otherwise take a second line and shift everything
 * below it.
 */
@Composable
fun ContestHeading(
    title: String,
    contest: Int?,
    date: LocalDate?,
    accent: Color,
    modifier: Modifier = Modifier,
    titleSize: TextUnit = 25.sp,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            autoSize = TextAutoSize.StepBased(
                minFontSize = titleSize * 0.6f,
                maxFontSize = titleSize,
            ),
            maxLines = 1,
            color = accent,
            textAlign = TextAlign.Center,
        )
        if (contest != null && date != null) {
            Text(
                text = stringResource(R.string.contest_number, contest) + " · " + date.asFullDate(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = MaterialTheme.typography.labelSmall.fontSize,
                ),
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}
