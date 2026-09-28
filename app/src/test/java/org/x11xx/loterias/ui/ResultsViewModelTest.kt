package org.x11xx.loterias.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.x11xx.loterias.data.ContestNotFoundException
import org.x11xx.loterias.data.ResultsCache
import org.x11xx.loterias.data.ResultsDataSource
import org.x11xx.loterias.data.ResultsJson
import org.x11xx.loterias.data.ResultsRepository
import org.x11xx.loterias.domain.Modality
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ResultsViewModelTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val dispatcher = StandardTestDispatcher()
    private val clock = MutableClock(Instant.parse("2026-09-28T15:00:00Z"))
    private val source = FakeSource()
    private lateinit var repository: ResultsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = ResultsRepository(source, ResultsCache(folder.root), io = dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(saved: SavedStateHandle = SavedStateHandle()) =
        ResultsViewModel(repository, saved, clock)

    @Test
    fun `a page open when the process is reclaimed loads again from disk`() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        val before = viewModel(saved)
        advanceUntilIdle()
        before.open(Modality.MEGA_SENA)
        before.step(-1)
        advanceUntilIdle()
        val shown = before.detail.value.contest

        source.offline = true
        val after = viewModel(saved.restored())
        advanceUntilIdle()

        assertEquals(Modality.MEGA_SENA, after.openGame.value)
        assertEquals(shown, after.detail.value.contest)
    }

    @Test
    fun `a page back on the newest contest follows a draw published while it is open`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()
            viewModel.open(Modality.MEGA_SENA)
            viewModel.step(-1)
            advanceUntilIdle()
            viewModel.step(1)
            advanceUntilIdle()

            val drawn = source.publish(Modality.MEGA_SENA)
            clock.advance(Duration.ofMinutes(2))
            viewModel.refreshDetail()
            advanceUntilIdle()

            assertEquals(drawn, viewModel.detail.value.contest)
        }

    @Test
    fun `coming back asks again only once the last check has gone stale`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.open(Modality.MEGA_SENA)
        val drawn = source.publish(Modality.MEGA_SENA)
        val asked = source.requests

        clock.advance(Duration.ofMinutes(2))
        viewModel.refreshIfStale()
        advanceUntilIdle()
        assertEquals(asked, source.requests)

        clock.advance(Duration.ofMinutes(4))
        viewModel.refreshIfStale()
        advanceUntilIdle()
        assertEquals(drawn, viewModel.detail.value.contest)
        assertEquals(drawn, (viewModel.games.value[Modality.MEGA_SENA] as GameState.Loaded).result.contest)
    }

    @Test
    fun `a contest that arrives after its page was left stays off the next one`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>().also { source.gate = it }
            viewModel.open(Modality.MEGA_SENA)
            viewModel.step(-1)
            runCurrent()

            viewModel.close()
            viewModel.open(Modality.QUINA)
            gate.complete(Unit)
            advanceUntilIdle()

            val page = viewModel.detail.value as DetailState.Loaded
            assertEquals(Modality.QUINA, page.result.modality)
        }

    @Test
    fun `a page that cannot load says so, and loads on retry`() = runTest(dispatcher) {
        source.offline = true
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.open(Modality.MEGA_SENA)
        advanceUntilIdle()
        assertEquals(DetailState.Failed, viewModel.detail.value)

        source.offline = false
        viewModel.refreshDetail()
        advanceUntilIdle()
        assertEquals(source.newest.getValue(Modality.MEGA_SENA), viewModel.detail.value.contest)
    }
}

private val DetailState.contest: Int?
    get() = (this as? DetailState.Loaded)?.result?.contest

/** What Android hands back of a view model's saved state once it recreates the process. */
private fun SavedStateHandle.restored(): SavedStateHandle =
    SavedStateHandle(keys().associateWith { get<Any?>(it) })

private class MutableClock(private var now: Instant) : Clock() {
    fun advance(by: Duration) {
        now += by
    }

    override fun instant(): Instant = now
    override fun getZone(): ZoneId = ZoneId.of("America/Sao_Paulo")
    override fun withZone(zone: ZoneId): Clock = this
}

/** The portal, answering from the saved fixtures with whatever contest number is asked for. */
private class FakeSource : ResultsDataSource {
    val newest = Modality.entries.associateWith { fixture(it)["numero"]!!.jsonPrimitive.int }
        .toMutableMap()
    var offline = false
    var requests = 0

    /** Holds every request for one contest until it completes. */
    var gate: CompletableDeferred<Unit>? = null

    /** Draws the next contest of [modality] and returns its number. */
    fun publish(modality: Modality): Int = (newest.getValue(modality) + 1).also { newest[modality] = it }

    override suspend fun latest(modality: Modality): String {
        requests++
        if (offline) throw IOException("offline")
        return body(modality, newest.getValue(modality))
    }

    override suspend fun contest(modality: Modality, contest: Int): String {
        requests++
        gate?.await()
        if (offline) throw IOException("offline")
        if (contest > newest.getValue(modality)) throw ContestNotFoundException("no contest $contest")
        return body(modality, contest)
    }

    private fun body(modality: Modality, contest: Int): String =
        JsonObject(fixture(modality) + ("numero" to JsonPrimitive(contest))).toString()

    private fun fixture(modality: Modality): JsonObject {
        val text = checkNotNull(javaClass.getResourceAsStream("/fixtures/${modality.slug}.json")) {
            "missing fixture for ${modality.slug}"
        }.bufferedReader().readText()
        return ResultsJson.parseToJsonElement(text).jsonObject
    }
}
