package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState

/**
 * Read-only Clocktower presentation over the *live* Session, not a second card-state cache.
 *
 * The backing cards own seat presentation during setup.
 * After Clocktower Session creation, every access resolves mechanics from Session.
 * A live list is essential: confirmation callbacks can read the updated Session in
 * the same frame, before Compose has recomposed the screen.
 */
internal class ClocktowerSessionPlayerCards(
    private val setupCards: List<PlayerCard>,
    private val currentSession: () -> ClocktowerSessionState?,
    private val language: String,
) : AbstractList<PlayerCard>() {
    override val size: Int get() = setupCards.size

    override fun get(index: Int): PlayerCard {
        val setupCard = setupCards[index]
        val session = currentSession() ?: return setupCard
        val seat = index + 1
        val player = requireNotNull(session.gameState.playerAt(seat)) {
            "Session must own Clocktower seat $seat."
        }
        check(player.name == setupCard.name) {
            "Clocktower presentation seat $seat differs from confirmed Session identity."
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
        return setupCard.copy(
            name = player.name,
            roleLabel = shown?.nameFor(language),
            actualRoleLabel = actual.nameFor(language),
            clocktowerTeam = actual.team,
            clocktowerRole = actual,
            clocktowerShownRole = shown,
            eliminatedRound = deathRound,
        )
    }
}
