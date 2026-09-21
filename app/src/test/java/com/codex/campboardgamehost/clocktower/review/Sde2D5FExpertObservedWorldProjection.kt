package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.EnumeratedWorld

/**
 * Evidence-side projection of a reconstructed canonical GameState into the exact mechanical world
 * used by the existing epistemic evaluator.
 *
 * This is not a second state model: actual/shown identities and poison state are copied from the
 * reconstructed canonical state, while the optional Red Herring is an already-committed setup fact.
 */
internal object Sde2D5FExpertObservedWorldProjection {
    private val drunk = RoleId("Drunk")

    fun from(
        game: GameState,
        redHerringSeat: Int? = null,
    ): EnumeratedWorld {
        val players = game.players.sortedBy { it.seat }
        return EnumeratedWorld(
            rolesBySeat = players.associate { player -> player.seat to player.actualRole },
            redHerringSeat = redHerringSeat,
            shownRolesBySeat = players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            abilityStatesBySeat = players.mapNotNull { player ->
                val state = when {
                    player.poisoned -> AbilityState.MALFUNCTIONING_POISONED
                    player.actualRole == drunk -> AbilityState.MALFUNCTIONING_DRUNK
                    else -> null
                }
                state?.let { player.seat to it }
            }.toMap(),
        )
    }
}
