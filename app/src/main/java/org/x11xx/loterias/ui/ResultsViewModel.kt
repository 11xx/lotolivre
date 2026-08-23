package org.x11xx.loterias.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
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

class ResultsViewModel(
    private val repository: ResultsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val policy: RefreshPolicy = RefreshPolicy(clock),
) : ViewModel() {

    private val _games = MutableStateFlow(
        Modality.entries.associateWith { GameState.Loading as GameState },
    )
    val games: StateFlow<Map<Modality, GameState>> = _games.asStateFlow()

    private val _updatedAt = MutableStateFlow<LocalTime?>(null)
    val updatedAt: StateFlow<LocalTime?> = _updatedAt.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _detail = MutableStateFlow<DetailState>(DetailState.Loading)
    val detail: StateFlow<DetailState> = _detail.asStateFlow()

    private val newestContest = mutableMapOf<Modality, Int>()
    private val lastFetched = mutableMapOf<Modality, Instant>()

    /** Keeps a refresh from opening eleven connections at once. */
    private val inFlight = Semaphore(permits = 4)

    init {
        refresh(userAsked = false)
    }

    /**
     * Renders whatever is on disk first, then asks the network only where
     * [RefreshPolicy] says an answer could differ from it.
     */
    fun refresh(userAsked: Boolean = true) {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            try {
                coroutineScope {
                    Modality.entries.forEach { modality ->
                        launch { refresh(modality, userAsked) }
                    }
                }
            } finally {
                _refreshing.value = false
            }
        }
    }

    private suspend fun refresh(modality: Modality, userAsked: Boolean) {
        val cached = runCatching { repository.cachedLatest(modality) }.getOrNull()
        cached?.let { publish(modality, it) }

        val allowed = policy.shouldFetch(
            lastFetched = lastFetched[modality],
            nextDraw = cached?.next?.date,
            userAsked = userAsked,
            hasCachedResult = cached != null,
        )
        if (!allowed) return

        inFlight.withPermit {
            runCatching { repository.refreshLatest(modality) }
                .onSuccess {
                    lastFetched[modality] = clock.instant()
                    publish(modality, it)
                    _updatedAt.value = LocalTime.now(clock)
                }
                .onFailure {
                    _games.update { states ->
                        if (states[modality] is GameState.Loaded) states
                        else states + (modality to GameState.Failed)
                    }
                }
        }
    }

    fun open(modality: Modality) {
        when (val state = _games.value[modality]) {
            is GameState.Loaded -> _detail.value = DetailState.Loaded(
                state.result,
                hasNext = hasNext(state.result),
            )
            else -> {
                _detail.value = DetailState.Loading
                viewModelScope.launch { load(modality) { repository.refreshLatest(modality) } }
            }
        }
    }

    fun step(delta: Int) {
        val current = _detail.value as? DetailState.Loaded ?: return
        val modality = current.result.modality
        val target = current.result.contest + delta
        if (target < 1) return
        _detail.value = DetailState.Loading
        viewModelScope.launch { load(modality) { repository.contest(modality, target) } }
    }

    private suspend fun load(modality: Modality, fetch: suspend () -> LotteryResult) {
        runCatching { fetch() }
            .onSuccess { result ->
                publish(modality, result)
                _detail.value = DetailState.Loaded(result, hasNext = hasNext(result))
            }
            .onFailure { failure ->
                _detail.value =
                    if (failure is ContestNotFoundException) DetailState.NotFound
                    else DetailState.Failed
            }
    }

    private fun publish(modality: Modality, result: LotteryResult) {
        newestContest[modality] = maxOf(newestContest[modality] ?: 0, result.contest)
        _games.update { states ->
            val known = (states[modality] as? GameState.Loaded)?.result
            if (known != null && known.contest > result.contest) states
            else states + (modality to GameState.Loaded(result))
        }
        val open = _detail.value
        if (open is DetailState.Loaded && open.result.modality == modality &&
            open.result.contest == result.contest
        ) {
            _detail.value = DetailState.Loaded(result, hasNext = hasNext(result))
        }
    }

    private fun hasNext(result: LotteryResult): Boolean =
        result.contest < (newestContest[result.modality] ?: result.contest)

}
