package org.x11xx.loterias.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.domain.Modality

/**
 * Offline-first: whatever was seen last renders immediately, and the network
 * refresh lands behind it. Historical contests are served from the cache
 * outright, since a drawn result cannot change.
 */
class ResultsRepository(
    private val source: ResultsDataSource,
    private val cache: ResultsCache,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {

    /** The newest contest already on disk, or null when nothing is cached. */
    suspend fun cachedLatest(modality: Modality): LotteryResult? = withContext(io) {
        val contest = cache.latestContest(modality) ?: return@withContext null
        cache.read(modality, contest)?.let { parse(modality, it) }
    }

    suspend fun refreshLatest(modality: Modality): LotteryResult = withContext(io) {
        val body = source.latest(modality)
        val result = parse(modality, body)
        cache.write(modality, result.contest, body)
        cache.writeLatest(modality, result.contest)
        result
    }

    suspend fun contest(modality: Modality, contest: Int): LotteryResult = withContext(io) {
        cache.read(modality, contest)?.let { return@withContext parse(modality, it) }
        val body = source.contest(modality, contest)
        val result = parse(modality, body)
        cache.write(modality, result.contest, body)
        result
    }

    private fun parse(modality: Modality, body: String): LotteryResult =
        ResultMapper.map(modality, ResultsJson.decodeFromString<ResultadoDto>(body))
}
