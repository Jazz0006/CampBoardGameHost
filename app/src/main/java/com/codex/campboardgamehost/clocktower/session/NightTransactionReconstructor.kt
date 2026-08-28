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
 * SNE-7 pure reconstruction seam.
 *
 * Durable authority remains GameState + ClocktowerNightCheckpoint. Reconstruction derives
 * effective same-night mechanics from those persisted inputs plus the canonical interaction plan.
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
        val successionResolution = resolveTroubleBrewingImpSelfKillSuccession(
            baseGameState = baseGameState,
            checkpoint = checkpoint,
            demonRoleId = demonRoleId,
        )
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
                canonicalSuccessorSeat != null &&
                demonSuccessorInteractionId in canonicalInteractionIds
            ) {
                listOf(
                    ResolvedNightMechanicalEvent.MechanicalDeath(
                        targetSeat = confirmedAttackDemonSeat,
                        effectiveAt = ClocktowerEffectiveNightCursor(
                            interactionId = demonSuccessorInteractionId,
                            boundary = ClocktowerInteractionBoundary.BEFORE,
                        ),
                    ),
                    ResolvedNightMechanicalEvent.RoleChanged(
                        targetSeat = canonicalSuccessorSeat,
                        roleId = demonRoleId,
                        effectiveAt = ClocktowerEffectiveNightCursor(
                            interactionId = demonSuccessorInteractionId,
                            boundary = ClocktowerInteractionBoundary.AFTER,
                        ),
                    ),
                )
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
        )
    }
}
