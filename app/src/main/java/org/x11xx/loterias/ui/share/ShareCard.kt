package org.x11xx.loterias.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.Draw
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.asFullDate
import org.x11xx.loterias.ui.contest.ContestBody
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.gameColors

/** The width the shared image is composed at, before the fixed density scales it. */
val ShareCardWidth = 400.dp

/**
 * A contest as a picture. The same blocks as the detail screen, under a
 * heading that names the game instead of the controls for walking between
 * contests — nobody can press those in an image.
 */
@Composable
fun ShareCard(result: LotteryResult, modifier: Modifier = Modifier) {
    val colors = gameColors(result.modality.hue, LocalDarkTheme.current)
    val draw = result.draw

    Column(
        modifier = modifier
            .width(ShareCardWidth)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = stringResource(result.modality.labelRes),
                style = MaterialTheme.typography.displaySmall,
                fontSize = 30.sp,
                color = colors.accent,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.contest_number, result.contest) +
                    " · " + result.date.asFullDate(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        // A short set of dezenas reads better centred under a centred heading;
        // the longer games fill the width anyway and stay left-aligned.
        ContestBody(
            result = result,
            centredDezenas = draw is Draw.Numbers && draw.numbers.size <= 6,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name) + " · " +
                    stringResource(R.string.about_repository),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            // The picture travels further than the app does, so it carries the
            // statement with it.
            Text(
                text = stringResource(R.string.share_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
