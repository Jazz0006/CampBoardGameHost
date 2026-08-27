package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState

/**
 * SNE-7 pure reconstruction seam scaffold.
 *
 * Durable authority remains GameState + ClocktowerNightCheckpoint. This scaffold intentionally
 * applies no same-night role changes yet; individual reconstruction behaviors are activated later.
 */
internal data class NightTransactionReconstruction(
    val currentInteractionId: ClocktowerInteractionId?,
    val effectiveState: ClocktowerEffectiveNightState,
)

internal object NightTransactionReconstructor {
    fun reconstruct(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        demonSuccessorInteractionId: ClocktowerInteractionId,
        demonRoleId: RoleId,
    ): NightTransactionReconstruction {
        @Suppress("UNUSED_VARIABLE")
        val contractInputs = demonSuccessorInteractionId to demonRoleId
        return NightTransactionReconstruction(
            currentInteractionId = canonicalInteractionIds.getOrNull(checkpoint.nightStepIndex),
            effectiveState = ClocktowerEffectiveNightState(
                effectiveAliveSeats = baseGameState.players
                    .filter { it.alive }
                    .map { it.seat }
                    .toSet(),
                effectiveRoleIdsBySeat = baseGameState.players.associate { it.seat to it.actualRole },
            ),
        )
    }
}
