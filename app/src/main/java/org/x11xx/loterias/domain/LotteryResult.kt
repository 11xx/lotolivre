package org.x11xx.loterias.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * One drawn contest. Results are immutable once drawn, which is why the cache
 * that stores them never needs invalidating.
 */
data class LotteryResult(
    val modality: Modality,
    val contest: Int,
    val date: LocalDate,
    val draw: Draw,
    val accumulated: Boolean,
    val accumulatedPrize: BigDecimal,
    val collected: BigDecimal,
    val tiers: List<PrizeTier>,
    val next: NextContest?,
    /** Timemania's heart team or Dia de Sorte's lucky month, when drawn. */
    val extra: String? = null,
) {
    /** The top prize tier — the one the list summarises. */
    val topTier: PrizeTier? get() = tiers.firstOrNull()
}

data class PrizeTier(
    val label: String,
    val winners: Int,
    val prize: BigDecimal,
)

data class NextContest(
    val contest: Int?,
    val date: LocalDate?,
    val estimatedPrize: BigDecimal,
)
