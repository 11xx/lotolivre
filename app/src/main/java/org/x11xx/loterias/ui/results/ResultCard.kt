package org.x11xx.loterias.ui.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.asDayMonth
import org.x11xx.loterias.ui.components.DrawContent
import org.x11xx.loterias.ui.summaryOf
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.gameColors

@Composable
fun ResultCard(
    result: LotteryResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = gameColors(result.modality.hue, LocalDarkTheme.current)
    val summary = summaryOf(result, LocalContext.current)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.wash)
            .border(BorderStroke(1.dp, colors.edge), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 14.dp, top = 13.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(result.modality.labelRes),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    R.string.contest_and_date,
                    result.contest,
                    result.date.asDayMonth(),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DrawContent(result.draw, colors, ballSize = 34.dp)

        val extra = result.extra
        val extraLabel = result.modality.extraToken
        if (extra != null && extraLabel != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(extraLabel.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = extra,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.softInk,
                )
            }
        }

        HorizontalDivider(color = colors.edge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = summary.status,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = true),
            )
            Text(
                text = summary.prize,
                style = MaterialTheme.typography.titleMedium,
                color = colors.accent,
                fontSize = 17.sp,
            )
        }
    }
}
