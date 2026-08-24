package org.x11xx.loterias.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.contest.ContestBody
import org.x11xx.loterias.ui.contest.ContestHeading
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.gameColors

/** The width the shared image is composed at, before the fixed density scales it. */
val ShareCardWidth = 400.dp

/**
 * A contest as a picture: the same heading and blocks as the detail screen,
 * without the controls for walking between contests — nobody can press those
 * in an image.
 *
 * The Surface is load-bearing rather than decorative. Text that names no
 * colour of its own takes the one the surface around it provides, and outside
 * a surface that is plain black — which is invisible against a dark theme.
 */
@Composable
fun ShareCard(result: LotteryResult, modifier: Modifier = Modifier) {
    val colors = gameColors(result.modality.hue, LocalDarkTheme.current)

    Surface(
        modifier = modifier.width(ShareCardWidth),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ContestHeading(
                title = stringResource(result.modality.labelRes),
                contest = result.contest,
                date = result.date,
                accent = colors.accent,
                titleSize = 30.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            ContestBody(result)
        }
    }
}
