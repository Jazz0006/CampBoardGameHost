package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId

/**
 * Pure restore composition for an unfinished Clocktower night.
 *
 * Durable authority remains the persisted checkpoint values plus [GameState]. This seam restores
 * the checkpoint and immediately derives same-night mechanical state without mutating either input.
 */
internal data class NightTransactionRestoreResult(
    val checkpoint: ClocktowerNightCheckpoint,
    val reconstruction: NightTransactionReconstruction,
)

internal object NightTransactionRestoreComposition {
    fun restore(
        persistedCheckpointValues: Map<String, Any?>,
        baseGameState: GameState,
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        demonSuccessorInteractionId: ClocktowerInteractionId,
        demonRoleId: RoleId,
    ): NightTransactionRestoreResult {
        val checkpoint = ClocktowerNightCheckpoint.fromPersistedValues(persistedCheckpointValues)
        return NightTransactionRestoreResult(
            checkpoint = checkpoint,
            reconstruction = NightTransactionReconstructor.reconstruct(
                baseGameState = baseGameState,
                checkpoint = checkpoint,
                canonicalInteractionIds = canonicalInteractionIds,
                demonSuccessorInteractionId = demonSuccessorInteractionId,
                demonRoleId = demonRoleId,
            ),
        )
    }
}
