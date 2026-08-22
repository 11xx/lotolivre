package org.x11xx.loterias.domain

/**
 * What a modality actually draws. Every game maps onto one of these once, so a
 * new game is one branch here and one renderer beside it rather than a special
 * case spread through the UI.
 */
sealed interface Draw {
    /** Mega-Sena, Lotofácil, Quina, Lotomania, Timemania, Dia de Sorte. */
    data class Numbers(val numbers: List<String>) : Draw

    /** Dupla Sena draws twice for the same ticket. */
    data class TwoDraws(val first: List<String>, val second: List<String>) : Draw

    /** +Milionária adds two trevos drawn from a separate pool. */
    data class NumbersWithClovers(val numbers: List<String>, val clovers: List<String>) : Draw

    /** Super Sete draws one digit per column, so position carries meaning. */
    data class DigitColumns(val digits: List<String>) : Draw

    /** Federal draws whole ticket numbers, ranked by prize. */
    data class Tickets(val tickets: List<Ticket>) : Draw

    /** Loteca draws nothing: it publishes the results of fourteen matches. */
    data class Matches(val matches: List<Match>) : Draw
}

data class Ticket(val place: Int, val number: String)

data class Match(
    val order: Int,
    val home: String,
    val away: String,
    val homeGoals: Int,
    val awayGoals: Int,
) {
    val outcome: MatchOutcome
        get() = when {
            homeGoals > awayGoals -> MatchOutcome.HOME
            awayGoals > homeGoals -> MatchOutcome.AWAY
            else -> MatchOutcome.DRAW
        }
}

enum class MatchOutcome { HOME, DRAW, AWAY }
