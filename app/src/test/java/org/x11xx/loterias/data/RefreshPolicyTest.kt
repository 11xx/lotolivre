package org.x11xx.loterias.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class RefreshPolicyTest {

    private val now: Instant = Instant.parse("2026-08-22T15:00:00Z")
    private val today: LocalDate = LocalDate.of(2026, 8, 22)
    private val policy = RefreshPolicy(Clock.fixed(now, ZoneId.of("UTC")))

    @Test
    fun `a game with nothing cached is always fetched`() {
        assertTrue(
            policy.shouldFetch(
                lastFetched = null,
                nextDraw = today.plusDays(5),
                userAsked = false,
                hasCachedResult = false,
            ),
        )
    }

    @Test
    fun `a draw still in the future is not asked about`() {
        assertFalse(
            policy.shouldFetch(
                lastFetched = null,
                nextDraw = today.plusDays(1),
                userAsked = false,
                hasCachedResult = true,
            ),
        )
    }

    @Test
    fun `the day of the draw is asked about`() {
        assertTrue(
            policy.shouldFetch(
                lastFetched = null,
                nextDraw = today,
                userAsked = false,
                hasCachedResult = true,
            ),
        )
    }

    @Test
    fun `a game that publishes no next date is asked about`() {
        assertTrue(
            policy.shouldFetch(
                lastFetched = null,
                nextDraw = null,
                userAsked = false,
                hasCachedResult = true,
            ),
        )
    }

    @Test
    fun `pulling the list answers a schedule that says wait`() {
        assertTrue(
            policy.shouldFetch(
                lastFetched = null,
                nextDraw = today.plusDays(3),
                userAsked = true,
                hasCachedResult = true,
            ),
        )
    }

    @Test
    fun `no game is asked twice inside the minimum interval, pull or not`() {
        val moments = listOf(now.minusSeconds(1), now.minusSeconds(59))
        moments.forEach { last ->
            assertFalse(
                "fetched at $last should still be rate limited",
                policy.shouldFetch(
                    lastFetched = last,
                    nextDraw = today,
                    userAsked = true,
                    hasCachedResult = true,
                ),
            )
        }
    }

    @Test
    fun `the interval expires`() {
        assertTrue(
            policy.shouldFetch(
                lastFetched = now.minus(Duration.ofMinutes(2)),
                nextDraw = today,
                userAsked = false,
                hasCachedResult = true,
            ),
        )
    }

    @Test
    fun `an empty cache outranks the interval only once it has passed`() {
        assertFalse(
            policy.shouldFetch(
                lastFetched = now.minusSeconds(10),
                nextDraw = null,
                userAsked = true,
                hasCachedResult = false,
            ),
        )
    }
}
