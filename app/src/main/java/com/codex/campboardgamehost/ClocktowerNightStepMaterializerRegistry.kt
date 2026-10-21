package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteraction
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteractionKind
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Pure renderer of Session-selected stable interactions; no ordering, wake or death decisions. */
internal class ClocktowerNightStepMaterializerRegistry(
    private val phase: ClocktowerNightFlowPhase,
    entries: Iterable<Entry>,
) {
    internal data class Entry(
        val identity: ClocktowerProductionNightStepIdentity,
        val build: () -> ClocktowerNightStepUi,
    )

    private val byInteractionId: Map<ClocktowerInteractionId, () -> ClocktowerNightStepUi>

    init {
        val identified = entries.map { entry ->
            entry.identity.interactionId(phase) to entry.build
        }
        require(identified.map { it.first }.distinct().size == identified.size) {
            "Clocktower night-step materializers must have unique interaction identities."
        }
        byInteractionId = identified.toMap()
    }

    fun materialize(
        projectedInteractions: List<ClocktowerHostInteraction>,
    ): List<ClocktowerNightStepUi> {
        require(projectedInteractions.all { it.phase == phase }) {
            "Clocktower night-step materialization cannot mix planner phases."
        }

        val actionableInteractions = projectedInteractions.filterNot { interaction ->
            interaction.kind == ClocktowerHostInteractionKind.SYSTEM_BOUNDARY
        }
        val projectedIds = actionableInteractions.map(ClocktowerHostInteraction::id)
        require(projectedIds.distinct().size == projectedIds.size) {
            "Clocktower projected materialization interaction IDs must be unique."
        }

        return actionableInteractions.map { interaction ->
            requireNotNull(byInteractionId[interaction.id]) {
                "Missing Clocktower night-step materializer for '${interaction.id.value}'."
            }.invoke().copy(interactionId = interaction.id)
        }
    }
}
