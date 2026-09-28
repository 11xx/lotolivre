package org.x11xx.loterias.data

import org.x11xx.loterias.domain.Draw
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.domain.Match
import org.x11xx.loterias.domain.Modality
import org.x11xx.loterias.domain.NextContest
import org.x11xx.loterias.domain.PrizeTier
import org.x11xx.loterias.domain.Ticket
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Turns the one wire shape into the per-game draw shapes, in one place. */
object ResultMapper {

    private val DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val NUL = Char(0)

    fun map(modality: Modality, dto: ResultadoDto): LotteryResult {
        val numbers = dto.listaDezenas.orEmpty()
        return LotteryResult(
            modality = modality,
            contest = dto.numero,
            date = parseDate(dto.dataApuracao)
                ?: throw IllegalArgumentException("contest ${dto.numero} has no draw date"),
            draw = drawOf(modality, dto, numbers),
            accumulated = dto.acumulado,
            accumulatedPrize = dto.valorAcumuladoProximoConcurso,
            collected = dto.valorArrecadado,
            tiers = dto.listaRateioPremio
                .sortedBy { it.faixa }
                .map { PrizeTier(tierLabel(modality, it), it.numeroDeGanhadores, it.valorPremio) },
            next = nextContestOf(dto),
            extra = sanitizeToken(dto.nomeTimeCoracaoMesSorte),
        )
    }

    /**
     * The portal's own description of a tier, with two exceptions. Federal's
     * ranked prizes come described as "1 acertos", "2 acertos", as if they were
     * hit counts, so they are named by the rank of the ticket they pay. Dupla
     * Sena's two draws come described alike, so each tier is marked with its
     * draw the way its row of dezenas is: each draw pays four tiers, numbered
     * one to four for the first and five to eight for the second.
     */
    private fun tierLabel(modality: Modality, tier: RateioDto): String {
        val description = tier.descricaoFaixa.trim()
        return when (modality) {
            Modality.FEDERAL -> "${tier.faixa}º prêmio"
            Modality.DUPLA_SENA -> "${if (tier.faixa <= DUPLA_SENA_TIERS_PER_DRAW) 1 else 2}º · $description"
            else -> description
        }
    }

    private const val DUPLA_SENA_TIERS_PER_DRAW = 4

    private fun drawOf(modality: Modality, dto: ResultadoDto, numbers: List<String>): Draw =
        when (modality) {
            Modality.DUPLA_SENA -> Draw.TwoDraws(numbers, dto.listaDezenasSegundoSorteio.orEmpty())
            Modality.MAIS_MILIONARIA ->
                Draw.NumbersWithClovers(numbers, dto.trevosSorteados.orEmpty())
            Modality.SUPER_SETE -> Draw.DigitColumns(numbers)
            Modality.FEDERAL -> Draw.Tickets(
                numbers.mapIndexed { index, number -> Ticket(index + 1, number) },
            )
            Modality.LOTECA -> Draw.Matches(
                dto.listaResultadoEquipeEsportiva.orEmpty()
                    .sortedBy { it.nuSequencial }
                    .map {
                        Match(
                            order = it.nuSequencial,
                            home = it.nomeEquipeUm.trim(),
                            away = it.nomeEquipeDois.trim(),
                            homeGoals = it.nuGolEquipeUm,
                            awayGoals = it.nuGolEquipeDois,
                        )
                    },
            )
            else -> Draw.Numbers(numbers)
        }

    private fun nextContestOf(dto: ResultadoDto): NextContest? {
        val date = parseDate(dto.dataProximoConcurso)
        val contest = dto.numeroConcursoProximo
        val estimate = dto.valorEstimadoProximoConcurso
        if (date == null && contest == null && estimate.signum() == 0) return null
        return NextContest(contest, date, estimate)
    }

    private fun parseDate(text: String?): LocalDate? {
        val trimmed = text?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return try {
            LocalDate.parse(trimmed, DATE)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /**
     * The heart-team field is padded with NUL bytes for the games it does not
     * apply to, and pads the team name itself with spaces before the state, so
     * a null check alone leaves both artefacts on screen.
     */
    private fun sanitizeToken(raw: String?): String? = raw
        ?.replace(NUL, ' ')
        ?.replace(Regex("\\s+"), " ")
        ?.replace(" /", "/")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
