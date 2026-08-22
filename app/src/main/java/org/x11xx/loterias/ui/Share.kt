package org.x11xx.loterias.ui

import android.content.Context
import android.content.Intent
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.Draw
import org.x11xx.loterias.domain.LotteryResult

/** A result as plain text, which is all a result is. */
fun Context.shareResult(result: LotteryResult, summary: ResultSummary) {
    val body = buildString {
        appendLine(
            getString(
                R.string.share_subject,
                getString(result.modality.labelRes),
                result.contest,
            ),
        )
        appendLine(result.date.asFullDate())
        appendLine(drawAsText(result))
        append(summary.status).append(" — ").append(summary.prize)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
}

private fun Context.drawAsText(result: LotteryResult): String {
    val drawn = when (val draw = result.draw) {
        is Draw.Numbers -> draw.numbers.joinToString(" ")
        is Draw.DigitColumns -> draw.digits.joinToString(" ")
        is Draw.TwoDraws -> buildString {
            append(getString(R.string.label_first_draw)).append(' ')
            append(draw.first.joinToString(" "))
            append('\n')
            append(getString(R.string.label_second_draw)).append(' ')
            append(draw.second.joinToString(" "))
        }
        is Draw.NumbersWithClovers -> buildString {
            append(draw.numbers.joinToString(" "))
            append('\n')
            append(getString(R.string.label_clovers)).append(' ')
            append(draw.clovers.joinToString(" "))
        }
        is Draw.Tickets -> draw.tickets.joinToString("\n") { "${it.place}º ${it.number}" }
        is Draw.Matches -> draw.matches.joinToString("\n") {
            "${it.order}. ${it.home} ${it.homeGoals}×${it.awayGoals} ${it.away}"
        }
    }
    val extra = result.extra
    val label = result.modality.extraToken
    return if (extra != null && label != null) {
        "$drawn\n${getString(label.labelRes)}: $extra"
    } else {
        drawn
    }
}
