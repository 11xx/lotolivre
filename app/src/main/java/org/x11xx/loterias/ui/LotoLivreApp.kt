package org.x11xx.loterias.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import org.x11xx.loterias.AppContainer
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.Modality
import org.x11xx.loterias.ui.about.AboutScreen
import org.x11xx.loterias.ui.detail.DetailScreen
import org.x11xx.loterias.ui.icons.InfoIcon
import org.x11xx.loterias.ui.icons.ListIcon
import org.x11xx.loterias.ui.results.ResultsScreen
import org.x11xx.loterias.ui.share.shareResultImage
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.LotoLivreTheme
import org.x11xx.loterias.ui.theme.ThemeMode

private enum class Screen { RESULTS, DETAIL, ABOUT }

@Composable
fun LotoLivreApp(container: AppContainer) {
    val themeMode by container.themePreference.mode.collectAsStateWithLifecycle()

    LotoLivreTheme(themeMode) {
        val viewModel: ResultsViewModel = viewModel(
            factory = viewModelFactory {
                initializer { ResultsViewModel(container.repository) }
            },
        )
        val games by viewModel.games.collectAsStateWithLifecycle()
        val detail by viewModel.detail.collectAsStateWithLifecycle()
        val updatedAt by viewModel.updatedAt.collectAsStateWithLifecycle()
        val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

        // Hoisted above the screen switch, so returning from a game lands
        // where the list was left rather than at the top.
        val listState = rememberLazyListState()
        val dark = LocalDarkTheme.current

        var screen by rememberSaveable { mutableStateOf(Screen.RESULTS.name) }
        var openGame by rememberSaveable { mutableStateOf<String?>(null) }
        val drawer = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        fun show(next: Screen) {
            screen = next.name
        }

        ModalNavigationDrawer(
            drawerState = drawer,
            drawerContent = {
                DrawerSheet(
                    current = Screen.valueOf(screen),
                    themeMode = themeMode,
                    onThemeMode = container.themePreference::set,
                    onResults = {
                        show(Screen.RESULTS)
                        scope.launch { drawer.close() }
                    },
                    onAbout = {
                        show(Screen.ABOUT)
                        scope.launch { drawer.close() }
                    },
                )
            },
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                when (Screen.valueOf(screen)) {
                    Screen.RESULTS -> ResultsScreen(
                        games = games,
                        updatedAt = updatedAt,
                        refreshing = refreshing,
                        listState = listState,
                        onOpenMenu = { scope.launch { drawer.open() } },
                        onOpenGame = { modality ->
                            openGame = modality.slug
                            viewModel.open(modality)
                            show(Screen.DETAIL)
                        },
                        onRefresh = { viewModel.refresh() },
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.safeDrawingPadding(),
                    )

                    Screen.DETAIL -> DetailScreen(
                        state = detail,
                        title = openGame
                            ?.let { Modality.ofSlug(it) }
                            ?.let { stringResource(it.labelRes) }
                            .orEmpty(),
                        onBack = { show(Screen.RESULTS) },
                        onStep = viewModel::step,
                        onShare = { result ->
                            scope.launch {
                                context.shareResultImage(
                                    result = result,
                                    summary = summaryOf(result, context),
                                    dark = dark,
                                )
                            }
                        },
                        modifier = Modifier.safeDrawingPadding(),
                    )

                    Screen.ABOUT -> AboutScreen(
                        onBack = { show(Screen.RESULTS) },
                        modifier = Modifier.safeDrawingPadding(),
                    )
                }
            }
        }

        BackHandler(enabled = Screen.valueOf(screen) != Screen.RESULTS) {
            show(Screen.RESULTS)
        }
    }
}

@Composable
private fun DrawerSheet(
    current: Screen,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    onResults: () -> Unit,
    onAbout: () -> Unit,
) {
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 10.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_results)) },
                    icon = { Icon(ListIcon, contentDescription = null) },
                    selected = current == Screen.RESULTS,
                    onClick = onResults,
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_about)) },
                    icon = { Icon(InfoIcon, contentDescription = null) },
                    selected = current == Screen.ABOUT,
                    onClick = onAbout,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.menu_theme),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
                SingleChoiceSegmentedButtonRow {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { onThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                        ) {
                            Text(stringResource(mode.labelRes))
                        }
                    }
                }
            }
        }
    }
}

private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }
