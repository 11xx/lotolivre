package org.x11xx.loterias.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.Modality
import org.x11xx.loterias.ui.GameState
import org.x11xx.loterias.ui.icons.MenuIcon
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val clock = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    games: Map<Modality, GameState>,
    updatedAt: LocalTime?,
    refreshing: Boolean,
    listState: LazyListState,
    onOpenMenu: () -> Unit,
    onOpenGame: (Modality) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Header(updatedAt = updatedAt, onOpenMenu = onOpenMenu)
            }
            items(Modality.entries, key = { it.slug }) { modality ->
                when (val state = games[modality]) {
                    is GameState.Loaded -> ResultCard(
                        result = state.result,
                        onClick = { onOpenGame(modality) },
                    )
                    GameState.Failed -> FailedCard(modality, onRetry)
                    else -> LoadingCard(modality)
                }
            }
        }
    }
}

@Composable
private fun Header(updatedAt: LocalTime?, onOpenMenu: () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The glyph sits centred in a 48dp target, so the button is pulled
            // left by that inset to put the icon on the same margin as the
            // title and the cards below it.
            IconButton(onClick = onOpenMenu, modifier = Modifier.offset(x = (-16).dp)) {
                Icon(MenuIcon, contentDescription = stringResource(R.string.menu_open))
            }
            Box(modifier = Modifier.weight(1f))
            if (updatedAt != null) {
                Text(
                    text = stringResource(R.string.label_updated, updatedAt.format(clock)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 10.dp),
        )
    }
}

@Composable
private fun LoadingCard(modality: Modality) {
    PlaceholderCard(modality) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FailedCard(modality: Modality, onRetry: () -> Unit) {
    PlaceholderCard(modality) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.error_load),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        }
    }
}

@Composable
private fun PlaceholderCard(modality: Modality, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(start = 14.dp, end = 14.dp, top = 13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(modality.labelRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}
