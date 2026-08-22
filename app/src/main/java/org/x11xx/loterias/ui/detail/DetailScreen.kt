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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.Draw
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.DetailState
import org.x11xx.loterias.ui.asDayMonth
import org.x11xx.loterias.ui.asFullDate
import org.x11xx.loterias.ui.asMoney
import org.x11xx.loterias.ui.components.DrawContent
import org.x11xx.loterias.ui.components.MatchMark
import org.x11xx.loterias.ui.icons.BackIcon
import org.x11xx.loterias.ui.icons.ForwardIcon
import org.x11xx.loterias.ui.icons.ShareIcon
import org.x11xx.loterias.ui.theme.GameColors
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.gameColors

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
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { loaded?.let { onShare(it.result) } },
                enabled = loaded != null,
            ) {
                Icon(ShareIcon, contentDescription = stringResource(R.string.action_share))
            }
        }

        ContestStepper(state = state, onStep = onStep)

        when (state) {
            is DetailState.Loaded -> Contest(state.result)
            DetailState.Loading -> Centred { CircularProgressIndicator(strokeWidth = 2.dp) }
            DetailState.NotFound -> Centred {
                Message(stringResource(R.string.error_contest_missing))
            }
            DetailState.Failed -> Centred { Message(stringResource(R.string.error_load)) }
        }
    }
}

@Composable
private fun ContestStepper(state: DetailState, onStep: (Int) -> Unit) {
    val loaded = state as? DetailState.Loaded
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StepButton(
            label = stringResource(R.string.action_previous),
            icon = BackIcon,
            enabled = loaded != null && loaded.result.contest > 1,
            onClick = { onStep(-1) },
            iconFirst = true,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = loaded?.let { stringResource(R.string.contest_number, it.result.contest) }.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = loaded?.result?.date?.asFullDate().orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (iconFirst) Icon(icon, null, Modifier.size(15.dp), tint = content)
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
        if (!iconFirst) Icon(icon, null, Modifier.size(15.dp), tint = content)
    }
}

@Composable
private fun Contest(result: LotteryResult) {
    val colors = gameColors(result.modality.hue, LocalDarkTheme.current)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val draw = result.draw) {
            is Draw.Matches -> MatchList(draw, colors)
            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.wash)
                    .border(BorderStroke(1.dp, colors.edge), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DrawContent(draw, colors, ballSize = 46.dp)
                val extra = result.extra
                val label = result.modality.extraToken
                if (extra != null && label != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(label.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = extra,
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.softInk,
                        )
                    }
                }
            }
        }

        Card {
            TierRow(
                stringResource(R.string.detail_tier),
                stringResource(R.string.detail_winners),
                stringResource(R.string.detail_prize),
                header = true,
            )
            result.tiers.forEachIndexed { index, tier ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                TierRow(
                    tier.label,
                    tier.winners.toString(),
                    if (tier.winners == 0) stringResource(R.string.detail_no_winner)
                    else tier.prize.asMoney(),
                    accent = if (tier.winners == 0 && index == 0) colors.accent else null,
                )
            }
        }

        val next = result.next
        if (next != null) {
            Card {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = stringResource(R.string.detail_accumulated),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = result.accumulatedPrize.asMoney(),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = next.contest
                                    ?.let { stringResource(R.string.detail_estimate, it) }
                                    ?: stringResource(R.string.detail_estimate_plain),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            next.date?.let {
                                Text(
                                    text = it.asFullDate(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = next.estimatedPrize.asMoney(),
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.accent,
                            fontSize = 20.sp,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.detail_collected),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = result.collected.asMoney(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MatchList(draw: Draw.Matches, colors: GameColors) {
    Card {
        draw.matches.forEachIndexed { index, match ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = match.order.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(width = 16.dp, height = 16.dp),
                )
                Text(
                    text = match.home,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${match.homeGoals}×${match.awayGoals}",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = match.away,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                MatchMark(match.outcome, colors, Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(16.dp),
            ),
    ) {
        content()
    }
}

@Composable
private fun TierRow(
    tier: String,
    winners: String,
    prize: String,
    header: Boolean = false,
    accent: Color? = null,
) {
    val style =
        if (header) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyLarge
    val colour =
        if (header) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = tier, style = style, color = colour, modifier = Modifier.weight(1.15f))
        Text(
            text = winners,
            style = style,
            color = colour,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.75f),
        )
        Text(
            text = prize,
            style = style,
            color = accent ?: colour,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f),
        )
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
