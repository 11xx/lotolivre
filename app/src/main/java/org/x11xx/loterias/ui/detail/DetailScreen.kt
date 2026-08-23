package org.x11xx.loterias.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.DetailState
import org.x11xx.loterias.ui.asFullDate
import org.x11xx.loterias.ui.contest.ContestBody
import org.x11xx.loterias.ui.contest.ContestHeading
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.gameColors
import org.x11xx.loterias.ui.icons.BackIcon
import org.x11xx.loterias.ui.icons.ForwardIcon
import org.x11xx.loterias.ui.icons.ShareIcon

@Composable
fun DetailScreen(
    state: DetailState,
    title: String,
    onBack: () -> Unit,
    onStep: (Int) -> Unit,
    onShare: (LotteryResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loaded = state as? DetailState.Loaded
    Column(modifier = modifier.fillMaxSize()) {
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

        ContestStepper(state = state, title = title, onStep = onStep)

        when (state) {
            is DetailState.Loaded -> ContestBody(
                result = state.result,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
            DetailState.Loading -> Centred { CircularProgressIndicator(strokeWidth = 2.dp) }
            DetailState.NotFound -> Centred {
                Message(stringResource(R.string.error_contest_missing))
            }
            DetailState.Failed -> Centred { Message(stringResource(R.string.error_load)) }
        }
    }
}

@Composable
private fun ContestStepper(state: DetailState, title: String, onStep: (Int) -> Unit) {
    val loaded = state as? DetailState.Loaded
    val accent = loaded
        ?.let { gameColors(it.result.modality.hue, LocalDarkTheme.current).accent }
        ?: MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StepButton(
            label = stringResource(R.string.action_previous),
            icon = BackIcon,
            enabled = loaded != null && loaded.result.contest > 1,
            onClick = { onStep(-1) },
            iconFirst = true,
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
        )
    }
}

@Composable
private fun StepButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    iconFirst: Boolean,
) {
    val content = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(12.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (iconFirst) Icon(icon, null, Modifier.size(15.dp), tint = content)
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
        if (!iconFirst) Icon(icon, null, Modifier.size(15.dp), tint = content)
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
