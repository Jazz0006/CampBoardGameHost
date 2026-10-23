package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState

/**
 * Read-only Clocktower presentation over the accepted Session snapshot, not a second card-state cache.
 *
 * Setup cards are used only before Session creation. An active or restored game
 * supplies its entire roster from Session, including size and names.
 * A live list is essential: confirmation callbacks can read the newly accepted snapshot in
 * the same frame, before Compose has recomposed the screen.
 */
internal class ClocktowerSessionPlayerCards(
    private val setupCards: List<PlayerCard>,
    private val currentSession: () -> ClocktowerSessionState?,
    private val language: String,
) : AbstractList<PlayerCard>() {
    override val size: Int get() = currentSession()?.gameState?.players?.size ?: setupCards.size

    override fun get(index: Int): PlayerCard {
        val session = currentSession() ?: return setupCards[index]
        if (index !in session.gameState.players.indices) throw IndexOutOfBoundsException(index.toString())
        val seat = index + 1
        val player = requireNotNull(session.gameState.playerAt(seat)) {
            "Session must own Clocktower seat $seat."
        }
        val actual = requireNotNull(clocktowerRoleByName(player.actualRole.value)) {
            "Unknown confirmed Clocktower role ${player.actualRole.value}."
        }
        val shown = player.shownRole?.value?.let { roleId ->
            requireNotNull(clocktowerRoleByName(roleId)) {
                "Unknown confirmed displayed Clocktower role $roleId."
            }
        }
        val deathRound = if (player.alive) null else {
            requireNotNull(session.actionTimeline.entries.firstOrNull { entry ->
                when (val fact = entry.fact) {
                    is ActionFact.Death -> fact.targetSeat == seat
                    is ActionFact.Execution -> fact.targetSeat == seat
                    else -> false
                }
            }?.point?.round) {
                "Dead Session seat $seat has no confirmed death or execution."
            }
        }
        return clocktowerPlayerCard(player.name, actual, shown, language, deathRound)
    }
}

/** One presentation builder for TB, NGJ and precommit shown-only cards. */
internal fun clocktowerPlayerCard(name: String, actual: ClocktowerRole?, shown: ClocktowerRole?,
    language: String, deathRound: Int? = null) = PlayerCard(
    name = name, roleLabel = shown?.nameFor(language), actualRoleLabel = actual?.nameFor(language),
    clocktowerTeam = (actual ?: shown)?.team, clocktowerRole = actual, clocktowerShownRole = shown,
    eliminatedRound = deathRound,
)

internal fun List<PlayerCard>.seatOf(name: String?): Int? =
    indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
