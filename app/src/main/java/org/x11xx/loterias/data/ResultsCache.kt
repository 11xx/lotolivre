package org.x11xx.loterias.data

import org.x11xx.loterias.domain.Modality
import java.io.File

/**
 * Raw responses kept on disk, keyed by modality and contest. A drawn result
 * never changes, so nothing here is ever invalidated — entries are only ever
 * added, and the newest contest number per modality is recorded beside them.
 */
class ResultsCache(private val dir: File) {

    fun read(modality: Modality, contest: Int): String? =
        entry(modality, contest).takeIf { it.isFile }?.readText()

    fun write(modality: Modality, contest: Int, body: String) {
        dir.mkdirs()
        entry(modality, contest).writeText(body)
    }

    fun latestContest(modality: Modality): Int? =
        pointer(modality).takeIf { it.isFile }?.readText()?.trim()?.toIntOrNull()

    fun writeLatest(modality: Modality, contest: Int) {
        dir.mkdirs()
        pointer(modality).writeText(contest.toString())
    }

    private fun entry(modality: Modality, contest: Int) = File(dir, "${modality.slug}-$contest.json")

    private fun pointer(modality: Modality) = File(dir, "${modality.slug}-latest")
}
