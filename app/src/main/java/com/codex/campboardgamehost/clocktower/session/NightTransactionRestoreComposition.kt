package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightCursor
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightStateProjector
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.ResolvedNightMechanicalEvent

/**
 * Pure restore composition for an unfinished Clocktower night.
 *
 * Durable authority remains [ClocktowerNightCheckpoint] plus [GameState]. Persisted values are
 * decoded to that typed checkpoint, then both restored and live production paths derive same-night
 * mechanical state through this seam without mutating their inputs.
 */
internal data class NightTransactionRestoreResult(
    val checkpoint: ClocktowerNightCheckpoint,
    val reconstruction: NightTransactionReconstruction,
)

internal data class NightTransactionReconstruction(
    val currentInteractionId: ClocktowerInteractionId?,
    val effectiveState: ClocktowerEffectiveNightState,
    val confirmedEvents: List<ResolvedNightMechanicalEvent> = emptyList(),
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
            reconstruction = compose(
                baseGameState = baseGameState,
                checkpoint = checkpoint,
                canonicalInteractionIds = canonicalInteractionIds,
                demonSuccessorInteractionId = demonSuccessorInteractionId,
                demonRoleId = demonRoleId,
            ),
        )
    }

    /** Shared production and restore-test entry for typed checkpoint reconstruction. */
    fun compose(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        demonSuccessorInteractionId: ClocktowerInteractionId,
        demonRoleId: RoleId,
    ): NightTransactionReconstruction {
        val currentInteractionId = canonicalInteractionIds.getOrNull(checkpoint.nightStepIndex)
        val baseAliveSeats = baseGameState.players
            .filter { it.alive }
            .map { it.seat }
            .toSet()
        val baseRoleIdsBySeat = baseGameState.players.associate { it.seat to it.actualRole }

        if (currentInteractionId == null) {
            return NightTransactionReconstruction(
                currentInteractionId = null,
                effectiveState = ClocktowerEffectiveNightState(
                    effectiveAliveSeats = baseAliveSeats,
                    effectiveRoleIdsBySeat = baseRoleIdsBySeat,
                ),
            )
        }

        val confirmedAttackDemonSeat = checkpoint.confirmedAttackTarget
            ?.let { targetName ->
                baseGameState.players.singleOrNull { player ->
                    player.name == targetName && player.actualRole == demonRoleId
                }?.seat
            }
        val impSelfKillResolution = resolveTroubleBrewingImpSelfKill(
            baseGameState = baseGameState,
            checkpoint = checkpoint,
            demonRoleId = demonRoleId,
        )
        val successionResolution = impSelfKillResolution.successionResolution
        val canonicalSuccessorSeat = when (successionResolution) {
            DemonSuccessionResolution.None -> null
            is DemonSuccessionResolution.Forced -> successionResolution.targetSeat
            is DemonSuccessionResolution.Choice -> checkpoint.confirmedDemonSuccessorTarget
                ?.let { targetName ->
                    baseGameState.players.singleOrNull { player -> player.name == targetName }?.seat
                }
                ?.takeIf { targetSeat -> targetSeat in successionResolution.targetSeats }
        }
        val confirmedEvents: List<ResolvedNightMechanicalEvent> =
            if (
                confirmedAttackDemonSeat != null &&
                impSelfKillResolution.demonActuallyDied &&
                demonSuccessorInteractionId in canonicalInteractionIds
            ) {
                buildList {
                    add(
                        ResolvedNightMechanicalEvent.MechanicalDeath(
                            targetSeat = confirmedAttackDemonSeat,
                            effectiveAt = ClocktowerEffectiveNightCursor(
                                interactionId = demonSuccessorInteractionId,
                                boundary = ClocktowerInteractionBoundary.BEFORE,
                            ),
                        ),
                    )
                    canonicalSuccessorSeat?.let { targetSeat ->
                        add(
                            ResolvedNightMechanicalEvent.RoleChanged(
                                targetSeat = targetSeat,
                                roleId = demonRoleId,
                                effectiveAt = ClocktowerEffectiveNightCursor(
                                    interactionId = demonSuccessorInteractionId,
                                    boundary = ClocktowerInteractionBoundary.AFTER,
                                ),
                            ),
                        )
                    }
                }
            } else {
                emptyList()
            }

        return NightTransactionReconstruction(
            currentInteractionId = currentInteractionId,
            effectiveState = ClocktowerEffectiveNightStateProjector.projectAt(
                baseAliveSeats = baseAliveSeats,
                canonicalInteractionIds = canonicalInteractionIds,
                confirmedEvents = confirmedEvents,
                cursor = ClocktowerEffectiveNightCursor(
                    interactionId = canonicalInteractionIds.last(),
                    boundary = ClocktowerInteractionBoundary.AFTER,
                ),
                baseRoleIdsBySeat = baseRoleIdsBySeat,
            ),
            confirmedEvents = confirmedEvents,
        )
    }
}
