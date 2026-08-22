package org.x11xx.loterias.ui

import android.content.Context
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.domain.Modality

/** The one line a card gives a game: what happened, and for how much. */
data class ResultSummary(val status: String, val prize: String)

fun summaryOf(result: LotteryResult, context: Context): ResultSummary {
    val next = result.next
    val top = result.topTier

    // Federal always pays; its tiers are ranked prizes rather than hit counts.
    if (result.modality == Modality.FEDERAL && top != null) {
        return ResultSummary(
            status = context.getString(R.string.status_first_prize),
            prize = top.prize.asMoney(),
        )
    }

    if (top != null && top.winners > 0) {
        return ResultSummary(
            status = context.resources.getQuantityString(
                R.plurals.status_winners,
                top.winners,
                top.winners,
                top.label,
            ),
            prize = top.prize.asMoney(),
        )
    }

    if (next != null) {
        val date = next.date
        val status = when {
            result.accumulated && date != null ->
                context.getString(R.string.status_accumulated_next, date.asDayMonth())
            result.accumulated -> context.getString(R.string.status_accumulated)
            date != null -> context.getString(R.string.status_next_draw, date.asDayMonth())
            else -> context.getString(R.string.status_accumulated)
        }
        return ResultSummary(status, next.estimatedPrize.asMoney())
    }

    return ResultSummary(
        status = context.getString(R.string.status_accumulated),
        prize = result.accumulatedPrize.asMoney(),
    )
}
