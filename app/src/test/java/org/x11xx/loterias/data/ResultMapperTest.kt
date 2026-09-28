package org.x11xx.loterias.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.x11xx.loterias.domain.Draw
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.domain.MatchOutcome
import org.x11xx.loterias.domain.Modality
import java.math.BigDecimal
import java.time.LocalDate

/**
 * The mapper is where every modality's difference is resolved, so it is tested
 * against a saved response from each of the eleven.
 */
class ResultMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun load(modality: Modality): LotteryResult {
        val text = checkNotNull(
            javaClass.getResourceAsStream("/fixtures/${modality.slug}.json"),
        ) { "missing fixture for ${modality.slug}" }.bufferedReader().readText()
        return ResultMapper.map(modality, json.decodeFromString<ResultadoDto>(text))
    }

    @Test
    fun `every modality maps to its own draw shape`() {
        val shapes = Modality.entries.associateWith { load(it).draw }

        assertTrue(shapes[Modality.MEGA_SENA] is Draw.Numbers)
        assertTrue(shapes[Modality.LOTOFACIL] is Draw.Numbers)
        assertTrue(shapes[Modality.QUINA] is Draw.Numbers)
        assertTrue(shapes[Modality.LOTOMANIA] is Draw.Numbers)
        assertTrue(shapes[Modality.TIMEMANIA] is Draw.Numbers)
        assertTrue(shapes[Modality.DIA_DE_SORTE] is Draw.Numbers)
        assertTrue(shapes[Modality.DUPLA_SENA] is Draw.TwoDraws)
        assertTrue(shapes[Modality.MAIS_MILIONARIA] is Draw.NumbersWithClovers)
        assertTrue(shapes[Modality.SUPER_SETE] is Draw.DigitColumns)
        assertTrue(shapes[Modality.FEDERAL] is Draw.Tickets)
        assertTrue(shapes[Modality.LOTECA] is Draw.Matches)
    }

    @Test
    fun `each game draws the number of dezenas it is supposed to`() {
        assertEquals(6, (load(Modality.MEGA_SENA).draw as Draw.Numbers).numbers.size)
        assertEquals(15, (load(Modality.LOTOFACIL).draw as Draw.Numbers).numbers.size)
        assertEquals(5, (load(Modality.QUINA).draw as Draw.Numbers).numbers.size)
        assertEquals(20, (load(Modality.LOTOMANIA).draw as Draw.Numbers).numbers.size)
        assertEquals(7, (load(Modality.TIMEMANIA).draw as Draw.Numbers).numbers.size)
    }

    @Test
    fun `mega sena keeps the dezenas as drawn, zero padded`() {
        val draw = load(Modality.MEGA_SENA).draw as Draw.Numbers
        assertEquals(listOf("04", "18", "22", "26", "31", "58"), draw.numbers)
    }

    @Test
    fun `dupla sena carries both draws`() {
        val draw = load(Modality.DUPLA_SENA).draw as Draw.TwoDraws
        assertEquals(6, draw.first.size)
        assertEquals(6, draw.second.size)
        assertEquals(listOf("10", "15", "18", "19", "46", "49"), draw.second)
    }

    @Test
    fun `mais milionaria carries the trevos alongside the dezenas`() {
        val draw = load(Modality.MAIS_MILIONARIA).draw as Draw.NumbersWithClovers
        assertEquals(6, draw.numbers.size)
        assertEquals(listOf("4", "6"), draw.clovers)
    }

    @Test
    fun `super sete draws one digit per column`() {
        val draw = load(Modality.SUPER_SETE).draw as Draw.DigitColumns
        assertEquals(7, draw.digits.size)
        assertTrue(draw.digits.all { it.length == 1 })
    }

    @Test
    fun `federal draws ranked ticket numbers`() {
        val draw = load(Modality.FEDERAL).draw as Draw.Tickets
        assertEquals(5, draw.tickets.size)
        assertEquals(listOf(1, 2, 3, 4, 5), draw.tickets.map { it.place })
        assertEquals("080574", draw.tickets.first().number)
        assertTrue(draw.tickets.all { it.number.length == 6 })
    }

    @Test
    fun `federal names its tiers by the rank they pay, not as hit counts`() {
        val tiers = load(Modality.FEDERAL).tiers
        assertEquals((1..5).map { "${it}º prêmio" }, tiers.map { it.label })
        assertEquals("15 acertos", load(Modality.LOTOFACIL).tiers.first().label)
    }

    @Test
    fun `dupla sena marks each tier with the draw it belongs to`() {
        assertEquals(
            listOf(6, 5, 4, 3).map { "1º · $it acertos" } + listOf(6, 5, 4, 3).map { "2º · $it acertos" },
            load(Modality.DUPLA_SENA).tiers.map { it.label },
        )
    }

    @Test
    fun `loteca has no dezenas at all and reads its matches instead`() {
        val result = load(Modality.LOTECA)
        val draw = result.draw as Draw.Matches
        assertEquals(14, draw.matches.size)
        assertEquals((1..14).toList(), draw.matches.map { it.order })

        val first = draw.matches.first()
        assertEquals("FLUMINENSE", first.home)
        assertEquals("PALMEIRAS", first.away)
        assertEquals(MatchOutcome.HOME, first.outcome)
        assertEquals(MatchOutcome.DRAW, draw.matches[4].outcome)
        assertEquals(MatchOutcome.AWAY, draw.matches[1].outcome)
    }

    @Test
    fun `the heart team field is dropped where it does not apply`() {
        assertNull(load(Modality.MEGA_SENA).extra)
        assertNull(load(Modality.LOTOFACIL).extra)
        assertNull(load(Modality.LOTECA).extra)
    }

    @Test
    fun `the heart team keeps its name without the padding the API sends`() {
        assertEquals("CONFIANCA/SE", load(Modality.TIMEMANIA).extra)
        assertEquals("Junho", load(Modality.DIA_DE_SORTE).extra)
    }

    @Test
    fun `prizes keep the centavos the server sent`() {
        val quina = load(Modality.QUINA)
        assertEquals(BigDecimal("16049664.67"), quina.topTier?.prize)
        assertEquals(BigDecimal("12069309.0"), quina.collected)
    }

    @Test
    fun `dates parse from the day-first form the API uses`() {
        assertEquals(LocalDate.of(2026, 8, 20), load(Modality.MEGA_SENA).date)
        assertEquals(LocalDate.of(2026, 8, 23), load(Modality.MEGA_SENA).next?.date)
    }

    @Test
    fun `a contest without a next draw date still reports its next contest`() {
        val federal = load(Modality.FEDERAL)
        assertNotNull(federal.next)
        assertNull(federal.next?.date)
        assertEquals(6094, federal.next?.contest)
    }

    @Test
    fun `prize tiers arrive in the order the game ranks them`() {
        val mega = load(Modality.MEGA_SENA)
        assertEquals(listOf("6 acertos", "5 acertos", "4 acertos"), mega.tiers.map { it.label })
        assertEquals(0, mega.topTier?.winners)
        assertTrue(mega.accumulated)
    }
}
