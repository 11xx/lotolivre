package org.x11xx.loterias.ui.contest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.Draw
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.asCount
import org.x11xx.loterias.ui.asFullDate
import org.x11xx.loterias.ui.asMoney
import org.x11xx.loterias.ui.components.DrawContent
import org.x11xx.loterias.ui.components.MatchMark
import org.x11xx.loterias.ui.theme.GameColors
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.gameColors

/**
 * Everything a contest says, below whatever heading the caller puts above it.
 * The detail screen scrolls it; the shared image lays it out at a fixed size.
 */
@Composable
fun ContestBody(
    result: LotteryResult,
    modifier: Modifier = Modifier,
    centredDezenas: Boolean = false,
) {
    val colors = gameColors(result.modality.hue, LocalDarkTheme.current)
    Column(
        modifier = modifier.fillMaxWidth(),
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
                DrawContent(draw, colors, ballSize = 46.dp, centred = centredDezenas)
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

        ContestCard {
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
                    tier.winners.asCount(),
                    if (tier.winners == 0) stringResource(R.string.detail_no_winner)
                    else tier.prize.asMoney(),
                    accent = if (tier.winners == 0 && index == 0) colors.accent else null,
                )
            }
        }

        val next = result.next
        if (next != null) {
            ContestCard {
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
    ContestCard {
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
private fun ContestCard(content: @Composable () -> Unit) {
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
