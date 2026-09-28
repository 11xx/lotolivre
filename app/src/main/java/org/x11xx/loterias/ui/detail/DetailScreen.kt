package org.x11xx.loterias.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.DetailState
import org.x11xx.loterias.ui.asFullDate
import org.x11xx.loterias.ui.components.readableInset
import org.x11xx.loterias.ui.contest.ContestBody
import org.x11xx.loterias.ui.contest.ContestHeading
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.gameColors
import org.x11xx.loterias.ui.icons.BackIcon
import org.x11xx.loterias.ui.icons.ForwardIcon
import org.x11xx.loterias.ui.icons.ShareIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailState,
    title: String,
    refreshing: Boolean,
    onBack: () -> Unit,
    onStep: (Int) -> Unit,
    onRefresh: () -> Unit,
    onShare: (LotteryResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loaded = state as? DetailState.Loaded
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // The contest keeps to a readable measure however wide the window, while
        // the whole width still scrolls and pulls.
        val inset = readableInset(maxWidth, minimum = 16.dp)
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(BackIcon, contentDescription = stringResource(R.string.action_back))
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { loaded?.let { onShare(it.result) } },
                    enabled = loaded != null,
                ) {
                    Icon(ShareIcon, contentDescription = stringResource(R.string.action_share))
                }
            }

            ContestStepper(state = state, title = title, onStep = onStep, inset = inset - 4.dp)

            // A page waiting on its first contest already shows a spinner of its own.
            PullToRefreshBox(
                isRefreshing = refreshing && loaded != null,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (state) {
                    is DetailState.Loaded -> ContestBody(
                        result = state.result,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = inset, end = inset, bottom = 24.dp),
                    )
                    DetailState.Loading -> Centred { CircularProgressIndicator(strokeWidth = 2.dp) }
                    DetailState.NotFound -> Centred {
                        Message(stringResource(R.string.error_contest_missing))
                    }
                    DetailState.Failed -> Centred {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Message(stringResource(R.string.error_load))
                            TextButton(onClick = onRefresh) { Text(stringResource(R.string.action_retry)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContestStepper(state: DetailState, title: String, onStep: (Int) -> Unit, inset: Dp) {
    val loaded = state as? DetailState.Loaded
    val accent = loaded
        ?.let { gameColors(it.result.modality.hue, LocalDarkTheme.current).accent }
        ?: MaterialTheme.colorScheme.onSurface
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // The steppers give up their labels before the heading between them
        // would lose its date. Text grows with the font size, so the row's room
        // is judged in text units rather than in dp.
        val compact = (maxWidth - inset * 2) / LocalDensity.current.fontScale < CompactStepperWidth
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = inset, end = inset, top = 2.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StepButton(
                label = stringResource(R.string.action_previous),
                icon = BackIcon,
                enabled = loaded != null && loaded.result.contest > 1,
                onClick = { onStep(-1) },
                iconFirst = true,
                compact = compact,
            )
            ContestHeading(
                title = title,
                contest = loaded?.result?.contest,
                date = loaded?.result?.date,
                accent = accent,
                modifier = Modifier.weight(1f),
            )
            StepButton(
                label = stringResource(R.string.action_next),
                icon = ForwardIcon,
                enabled = loaded?.hasNext == true,
                onClick = { onStep(1) },
                iconFirst = false,
                compact = compact,
            )
        }
    }
}

/** Below this much room in the row, in text units, the step buttons show their arrows alone. */
private val CompactStepperWidth = 380.dp

@Composable
private fun StepButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    iconFirst: Boolean,
    compact: Boolean,
) {
    val content = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f)
    Row(
        modifier = Modifier
            .then(if (compact) Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp) else Modifier)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(12.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        if (compact) {
            Icon(icon, label, Modifier.size(15.dp), tint = content)
        } else {
            if (iconFirst) Icon(icon, null, Modifier.size(15.dp), tint = content)
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
            if (!iconFirst) Icon(icon, null, Modifier.size(15.dp), tint = content)
        }
    }
}

@Composable
private fun Centred(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
