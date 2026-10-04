package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId

internal data class MayorRedirectDecisionDomain(
    val mayorSeat: Int,
    val legalTargetSeats: Set<Int>,
) {
    init {
        require(mayorSeat > 0) { "Mayor seat must be positive." }
        require(mayorSeat in legalTargetSeats) { "Mayor direct-death seat must remain in the decision domain." }
        require(legalTargetSeats.all { it > 0 }) { "Mayor redirect target seats must be positive." }
    }
}

internal object MayorRedirectLegalDomain {
    private val mayorRole = RoleId("Mayor")

    fun resolve(game: GameState, mayorSeat: Int): MayorRedirectDecisionDomain {
        val mayor = requireNotNull(game.playerAt(mayorSeat)) { "Mayor seat is absent from game state." }
        require(mayor.actualRole == mayorRole && mayor.alive) {
            "Mayor redirect domain requires a living actual Mayor at the source seat."
        }
        val legalSeats = game.players.mapNotNullTo(linkedSetOf()) { target ->
            target.seat.takeIf {
                target.seat == mayorSeat ||
                    MayorRedirectLegality.canReceiveRedirect(
                        targetIsDemon = target.actualType == CharacterType.DEMON,
                    )
            }
        }
        return MayorRedirectDecisionDomain(
            mayorSeat = mayorSeat,
            legalTargetSeats = legalSeats,
        )
    }
}

/**
 * Current product restriction for Trouble Brewing automatic hosting.
 *
 * Official Mayor rules can redirect a night death to another player, including a Demon. The app
 * intentionally excludes Demon targets so the current Trouble Brewing host does not create a
 * non-self Demon-death succession path before generic cross-script Demon succession is supported.
 */
internal object MayorRedirectLegality {
    fun canReceiveRedirect(targetIsDemon: Boolean): Boolean = !targetIsDemon
}
