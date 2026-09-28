package org.x11xx.loterias.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.x11xx.loterias.data.ContestNotFoundException
import org.x11xx.loterias.data.RefreshPolicy
import org.x11xx.loterias.data.ResultsRepository
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.domain.Modality
import java.time.Clock
import java.time.Instant
import java.time.LocalTime

sealed interface GameState {
    data object Loading : GameState
    data class Loaded(val result: LotteryResult) : GameState
    data object Failed : GameState
}

sealed interface DetailState {
    data object Loading : DetailState
    data class Loaded(val result: LotteryResult, val hasNext: Boolean) : DetailState
    data object NotFound : DetailState
    data object Failed : DetailState
}

/**
 * The list of games and the one game page.
 *
 * The page asks either for one contest or for its game's newest, and that
 * request lives in [SavedStateHandle]: a process reclaimed in the background
 * reopens on the same page and loads it again. A page on the newest follows it,
 * so a draw published while the page is open replaces what it shows.
 */
class ResultsViewModel(
    private val repository: ResultsRepository,
    private val saved: SavedStateHandle,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val policy: RefreshPolicy = RefreshPolicy(clock),
) : ViewModel() {

    private val _games = MutableStateFlow(
        Modality.entries.associateWith { GameState.Loading as GameState },
    )
    val games: StateFlow<Map<Modality, GameState>> = _games.asStateFlow()

    private val _updatedAt = MutableStateFlow<LocalTime?>(null)
    val updatedAt: StateFlow<LocalTime?> = _updatedAt.asStateFlow()

    private val _refreshing = MutableStateFlow(emptySet<Modality>())

    /** The games whose newest contest is being asked of the portal right now. */
    val refreshing: StateFlow<Set<Modality>> = _refreshing.asStateFlow()

    private val _openGame = MutableStateFlow(saved.get<String>(OPEN_GAME)?.let(Modality::ofSlug))

    /** The game on the detail page, or null while no page is open. */
    val openGame: StateFlow<Modality?> = _openGame.asStateFlow()

    private val _detail = MutableStateFlow<DetailState>(DetailState.Loading)
    val detail: StateFlow<DetailState> = _detail.asStateFlow()

    private val lastAsked = mutableMapOf<Modality, Instant>()
    private var lastChecked: Instant? = null
    private var pageLoad: Job? = null

    /** Keeps a refresh from opening eleven connections at once. */
    private val inFlight = Semaphore(permits = 4)

    init {
        _openGame.value?.let { load(it, saved.get<Int>(OPEN_CONTEST)) }
        refresh(userAsked = false)
    }

    /**
     * Renders whatever is on disk first, then asks the network only where
     * [RefreshPolicy] says an answer could differ from it.
     */
    fun refresh(userAsked: Boolean = true) {
        lastChecked = clock.instant()
        Modality.entries.forEach { modality ->
            viewModelScope.launch { refresh(modality, userAsked) }
        }
    }

    /** The automatic check again, once [RefreshPolicy] finds the last one stale. */
    fun refreshIfStale() {
        if (policy.isStale(lastChecked)) refresh(userAsked = false)
    }

    private suspend fun refresh(modality: Modality, userAsked: Boolean) {
        val known = newest(modality)
            ?: runCatching { repository.cachedLatest(modality) }.getOrNull()
                ?.also { publish(modality, it) }

        if (modality in _refreshing.value) return
        val allowed = policy.shouldFetch(
            lastAsked = lastAsked[modality],
            nextDraw = known?.next?.date,
            userAsked = userAsked,
            hasCachedResult = known != null,
        )
        if (!allowed) {
            failWaitingPage(modality)
            return
        }

        _refreshing.update { it + modality }
        try {
            inFlight.withPermit {
                lastAsked[modality] = clock.instant()
                runCatching { repository.refreshLatest(modality) }
                    .onSuccess {
                        publish(modality, it)
                        _updatedAt.value = LocalTime.now(clock)
                    }
                    .onFailure {
                        _games.update { states ->
                            if (states[modality] is GameState.Loaded) states
                            else states + (modality to GameState.Failed)
                        }
                        failWaitingPage(modality)
                    }
            }
        } finally {
            _refreshing.update { it - modality }
        }
    }

    fun open(modality: Modality) {
        saved[OPEN_GAME] = modality.slug
        _openGame.value = modality
        load(modality, contest = null)
    }

    fun close() {
        pageLoad?.cancel()
        saved.remove<String>(OPEN_GAME)
        saved.remove<Int>(OPEN_CONTEST)
        _openGame.value = null
        _detail.value = DetailState.Loading
    }

    fun step(delta: Int) {
        val page = _detail.value as? DetailState.Loaded ?: return
        val modality = page.result.modality
        val target = page.result.contest + delta
        if (target < 1) return
        // Arriving at the newest contest follows it again.
        load(modality, target.takeUnless { it == newest(modality)?.contest })
    }

    /** Asks after the open game's newest contest, or loads a failed page again. */
    fun refreshDetail() {
        val modality = _openGame.value ?: return
        when (_detail.value) {
            is DetailState.Loaded -> viewModelScope.launch { refresh(modality, userAsked = true) }
            DetailState.Failed -> load(modality, saved.get<Int>(OPEN_CONTEST))
            DetailState.Loading, DetailState.NotFound -> Unit
        }
    }

    /**
     * Puts [contest] of [modality] on the page, or its newest when [contest] is
     * null. A newest nobody has seen yet arrives through the game's own refresh,
     * which [publish]es it or fails the page.
     */
    private fun load(modality: Modality, contest: Int?) {
        saved[OPEN_CONTEST] = contest
        pageLoad?.cancel()
        val newest = newest(modality)
        if (contest == null && newest != null) {
            show(newest)
            return
        }
        _detail.value = DetailState.Loading
        if (contest == null) {
            viewModelScope.launch { refresh(modality, userAsked = false) }
            return
        }
        pageLoad = viewModelScope.launch {
            val outcome = runCatching { repository.contest(modality, contest) }
            // A blocking request runs to its answer even once the page is left,
            // and an error it ends in would outrank the cancellation.
            ensureActive()
            outcome
                .onSuccess(::show)
                .onFailure { failure ->
                    _detail.value =
                        if (failure is ContestNotFoundException) DetailState.NotFound
                        else DetailState.Failed
                }
        }
    }

    private fun publish(modality: Modality, result: LotteryResult) {
        val newest = newest(modality)?.takeIf { it.contest > result.contest } ?: result
        _games.update { it + (modality to GameState.Loaded(newest)) }

        val page = _detail.value
        when {
            followsNewest(modality) -> show(newest)
            page is DetailState.Loaded && page.result.modality == modality -> show(page.result)
        }
    }

    private fun followsNewest(modality: Modality): Boolean =
        _openGame.value == modality && saved.get<Int>(OPEN_CONTEST) == null

    /** A page waiting on [modality]'s newest, with no answer coming, has failed. */
    private fun failWaitingPage(modality: Modality) {
        if (followsNewest(modality) && _detail.value == DetailState.Loading) {
            _detail.value = DetailState.Failed
        }
    }

    private fun show(result: LotteryResult) {
        _detail.value = DetailState.Loaded(result, hasNext = hasNext(result))
    }

    private fun newest(modality: Modality): LotteryResult? =
        (_games.value[modality] as? GameState.Loaded)?.result

    private fun hasNext(result: LotteryResult): Boolean =
        result.contest < (newest(result.modality)?.contest ?: result.contest)

    private companion object {
        const val OPEN_GAME = "open_game"
        const val OPEN_CONTEST = "open_contest"
    }
}
