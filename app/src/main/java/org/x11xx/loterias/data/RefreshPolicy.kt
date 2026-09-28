package org.x11xx.loterias.data

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/**
 * When it is worth asking the portal again.
 *
 * A drawn result never changes, so a request can only ever discover a contest
 * that has since been drawn. Two rules follow from that, and together they keep
 * ordinary use far below anything the portal could object to:
 *
 * - A game whose next draw is still in the future is not asked about at all,
 *   because nothing can have been published yet.
 * - No game is asked about twice inside [minimumInterval], however often the
 *   list is pulled.
 *
 * Pulling the list down is an explicit ask and answers the first rule, since
 * the reader may know something the schedule does not — a draw running late,
 * or a date the previous response got wrong. It never bypasses the second.
 *
 * Nothing asks on a timer. Coming back to the app runs the automatic check
 * again once the last one is older than [staleAfter], so a list left in the
 * background across a draw catches up without being pulled, and the two rules
 * above still decide which games that check actually asks about.
 */
class RefreshPolicy(
    private val clock: Clock,
    private val minimumInterval: Duration = Duration.ofMinutes(1),
    private val staleAfter: Duration = Duration.ofMinutes(5),
) {
    fun isStale(lastChecked: Instant?): Boolean =
        lastChecked == null || Duration.between(lastChecked, clock.instant()) >= staleAfter

    fun shouldFetch(
        lastFetched: Instant?,
        nextDraw: LocalDate?,
        userAsked: Boolean,
        hasCachedResult: Boolean,
    ): Boolean {
        if (lastFetched != null &&
            Duration.between(lastFetched, clock.instant()) < minimumInterval
        ) {
            return false
        }
        if (!hasCachedResult) return true
        if (userAsked) return true
        if (nextDraw == null) return true
        return !nextDraw.isAfter(LocalDate.now(clock))
    }
}
